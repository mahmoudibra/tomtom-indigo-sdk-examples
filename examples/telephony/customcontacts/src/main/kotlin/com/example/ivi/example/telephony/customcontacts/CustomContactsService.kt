/*
 * Copyright © 2020 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

package com.example.ivi.example.telephony.customcontacts

import android.graphics.Bitmap
import android.graphics.Color
import com.tomtom.ivi.platform.contacts.api.common.model.Address
import com.tomtom.ivi.platform.contacts.api.common.model.AddressType
import com.tomtom.ivi.platform.contacts.api.common.model.Contact
import com.tomtom.ivi.platform.contacts.api.common.model.ContactId
import com.tomtom.ivi.platform.contacts.api.common.model.PhoneNumber
import com.tomtom.ivi.platform.contacts.api.common.model.PhoneNumberType
import com.tomtom.ivi.platform.telecom.api.common.model.PhoneBookSynchronizationStatus
import com.tomtom.ivi.platform.contacts.api.service.contacts.ContactsServiceBase
import com.tomtom.ivi.platform.framework.api.ipc.iviservice.IviServiceHostContext
import com.tomtom.ivi.platform.framework.api.ipc.iviservice.mirrormap.MutableMirrorableMap

/**
 * A custom contacts service that contains two contacts. Only the contact with the id `1` has an
 * image.
 */
internal class CustomContactsService(iviServiceHostContext: IviServiceHostContext) :
    ContactsServiceBase(iviServiceHostContext) {

    // A mutable list of contacts that can be updated with the contactsSource changes.
    private val mutableContacts = MutableMirrorableMap<ContactId, Contact>()

    // A mutable contacts data source that can be updated with the contactsSource changes.
    private val mutableContactsDataSource = MutableCustomContactsDataSource()

    // The source of contacts.
    internal val contactsSource = mutableListOf(
        Contact(
            contactId = ContactId("1"),
            displayName = "John Smith",
            initials = "JS",
            givenName = "John",
            familyName = "Smith",
            companyName = "Company Name",
            phoneNumbers = listOf(PhoneNumber("+281111111", PhoneNumberType.Main)),
            defaultPhoneNumberIndex = 0,
            addresses = listOf(Address("45 Some Street, SomeCity, SC 10000", AddressType.Home)),
            defaultAddressIndex = 0,
            favorite = true,
            primarySortKey = "John Smith",
            alternativeSortKey = "Smith John"
        ),
        Contact(
            contactId = ContactId("2"),
            displayName = "Kelly Goodwin",
            initials = "KG",
            givenName = "Kelly",
            familyName = "Goodwin",
            companyName = "Another Company Name",
            phoneNumbers = listOf(PhoneNumber("+2822222222", PhoneNumberType.Mobile)),
            defaultPhoneNumberIndex = 0,
            addresses = listOf(
                Address(
                    "45 Some Other Street, SomeOtherCity, SC 12000",
                    AddressType.Work
                )
            ),
            defaultAddressIndex = 0,
            favorite = false,
            primarySortKey = "Kelly Goodwin",
            alternativeSortKey = "Goodwin Kelly"
        ),
    )

    override fun onCreate() {
        super.onCreate()
        // Initialize the synchronization status.
        phoneBookSynchronizationStatus = PhoneBookSynchronizationStatus.NO_CONNECTED_DEVICES
        // Bind the contacts property to an empty mutable map.
        contacts = mutableContacts
        // Bind the contactsDataSource property to an empty mutable contacts data source.
        contactsDataSource = mutableContactsDataSource
        // Set the service to ready. Now clients can call the service's APIs.
        serviceReady = true
        // The source of contacts is ready and synchronization starts.
        phoneBookSynchronizationStatus = PhoneBookSynchronizationStatus.SYNCHRONIZATION_IN_PROGRESS
        contactsSource.forEach {
            // Updating the contacts property with contacts from the source.
            mutableContacts[it.contactId] = it
            // Updating the contactsDataSource property with contacts from the source.
            mutableContactsDataSource.addOrUpdateContact(it)
        }
    }

    override fun onDestroy() {
        // Put cleanup code here, if necessary.
        super.onDestroy()
    }

    override suspend fun getImage(contactId: ContactId): Bitmap? {
        return when (contactId) {
            // Return a bitmap for contactId 1.
            ContactId("1") -> Bitmap.createBitmap(
                IntArray(50 * 50) { Color.BLACK }, 50, 50, Bitmap.Config.ARGB_8888
            )
            // Return null for other contacts.
            else -> null
        }
    }
}
