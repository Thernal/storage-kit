package io.thernal.storagekit.storage.impl.data.secure

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitAll
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnAttributes
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * The iOS [SecretBackend]: one generic-password Keychain item per value, grouped under [service].
 *
 * Items are `AfterFirstUnlockThisDeviceOnly`: readable in the background once the device has been
 * unlocked since boot (a token refresh can run), and never copied to another device by a backup —
 * a restored device starts signed out rather than with credentials it did not issue.
 */
@OptIn(ExperimentalForeignApi::class)
class KeychainBackend(
    private val service: String,
) : SecretBackend {
    override suspend fun read(name: String): String? {
        return withContext(Dispatchers.IO) {
            copyMatching(item(name) + (kSecReturnData to kCFBooleanTrue) + (kSecMatchLimit to kSecMatchLimitOne))
                ?.let { result -> CFBridgingRelease(result) as? NSData }
                ?.let { data -> NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString() }
        }
    }

    override suspend fun write(
        name: String,
        value: String,
    ) {
        withContext(Dispatchers.IO) {
            delete(item(name))
            val data = NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding)
                ?: throw KeychainException("encoding", -1)
            val dataRef = CFBridgingRetain(data)
            try {
                val attributes = item(name) +
                    (kSecValueData to dataRef) +
                    (kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
                withDictionary(attributes) { query -> check("add", SecItemAdd(query, null)) }
            } finally {
                CFRelease(dataRef)
            }
        }
    }

    override suspend fun remove(name: String) {
        withContext(Dispatchers.IO) { delete(item(name)) }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) { delete(serviceItems()) }
    }

    override suspend fun names(): Set<String> {
        return withContext(Dispatchers.IO) {
            val query = serviceItems() +
                (kSecReturnAttributes to kCFBooleanTrue) +
                (kSecMatchLimit to kSecMatchLimitAll)
            val found = copyMatching(query)
                ?.let { result -> CFBridgingRelease(result) as? List<*> }
                .orEmpty()
            val account = CFBridgingRelease(CFBridgingRetain(CFBridgingRelease(kSecAttrAccount)))
            found.mapNotNullTo(mutableSetOf()) { attributes -> (attributes as? Map<*, *>)?.get(account) as? String }
        }
    }

    private fun serviceItems(): List<Pair<CFTypeRef?, CFTypeRef?>> {
        return listOf(kSecClass to kSecClassGenericPassword, kSecAttrService to service.retained())
    }

    private fun item(name: String): List<Pair<CFTypeRef?, CFTypeRef?>> {
        return serviceItems() + (kSecAttrAccount to name.retained())
    }

    private fun copyMatching(query: List<Pair<CFTypeRef?, CFTypeRef?>>): CFTypeRef? {
        return withDictionary(query) { dictionary ->
            memScoped {
                val result = alloc<CFTypeRefVar>()
                when (val status = SecItemCopyMatching(dictionary, result.ptr)) {
                    errSecSuccess -> result.value
                    errSecItemNotFound -> null
                    else -> throw KeychainException("read", status)
                }
            }
        }
    }

    private fun delete(query: List<Pair<CFTypeRef?, CFTypeRef?>>) {
        withDictionary(query) { dictionary ->
            val status = SecItemDelete(dictionary)
            if (status != errSecItemNotFound) {
                check("delete", status)
            }
        }
    }

    private fun <R> withDictionary(
        entries: List<Pair<CFTypeRef?, CFTypeRef?>>,
        block: (CFDictionaryRef?) -> R,
    ): R {
        val dictionary = CFDictionaryCreateMutable(
            null,
            entries.size.toLong(),
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr,
        )
        entries.forEach { (key, value) -> CFDictionaryAddValue(dictionary, key, value) }
        return try {
            block(dictionary)
        } finally {
            CFRelease(dictionary)
        }
    }

    private fun check(
        operation: String,
        status: Int,
    ) {
        if (status != errSecSuccess) {
            throw KeychainException(operation, status)
        }
    }

    private fun String.retained(): CFTypeRef? {
        return CFBridgingRetain(NSString.create(string = this))
    }
}
