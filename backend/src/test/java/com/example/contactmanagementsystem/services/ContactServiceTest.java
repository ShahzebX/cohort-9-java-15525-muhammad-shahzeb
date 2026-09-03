package com.example.contactmanagementsystem.services;

import com.example.contactmanagementsystem.*;
import com.example.contactmanagementsystem.dto.ContactRequest;
import com.example.contactmanagementsystem.dto.EmailRequest;
import com.example.contactmanagementsystem.dto.PhoneRequest;
import com.example.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    private User user;
    private Contact contact;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        contact = new Contact();
        contact.setId(1);
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setUser(user);
    }

    @Test
    void createContact_shouldSetUserAndSave() {
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contact result = contactService.createContact(new ContactRequest(), user);

        assertEquals(user, result.getUser());
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void createContact_shouldRejectNullUser() {
        assertThrows(IllegalArgumentException.class,
                () -> contactService.createContact(new ContactRequest(), null));

        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void createContact_shouldLinkNestedEmailAndPhoneToContact() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("nested@example.com");
        PhoneRequest phoneRequest = new PhoneRequest();
        phoneRequest.setPhoneNumber("555-1234");

        ContactRequest newContact = new ContactRequest();
        newContact.setEmails(new java.util.ArrayList<>(java.util.List.of(emailRequest)));
        newContact.setPhones(new java.util.ArrayList<>(java.util.List.of(phoneRequest)));

        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contact result = contactService.createContact(newContact, user);

        assertEquals(1, result.getEmails().size());
        assertEquals(1, result.getPhones().size());
        assertSame(result, result.getEmails().get(0).getContact());
        assertSame(result, result.getPhones().get(0).getContact());
        assertEquals("nested@example.com", result.getEmails().get(0).getEmail());
        assertEquals("555-1234", result.getPhones().get(0).getPhoneNumber());
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void getContactById_shouldReturnContactWhenOwnedByUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));

        Contact result = contactService.getContactById(1, user);

        assertEquals("John", result.getFirstName());
    }

    @Test
    void getContactById_shouldThrowWhenNotFoundForUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.getContactById(1, user));
    }

    @Test
    void deleteContact_shouldDeleteWhenOwnedByUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));

        contactService.deleteContact(1, user);

        verify(contactRepository).delete(contact);
    }

    @Test
    void deleteContact_shouldThrowWhenNotFoundForUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.deleteContact(1, user));
    }

    @Test
    void updateContact_shouldUpdateWhenOwnedByUser() {
        ContactRequest updatedData = new ContactRequest();
        updatedData.setFirstName("Jane");
        updatedData.setLastName("Smith");
        updatedData.setEmails(new java.util.ArrayList<>());
        updatedData.setPhones(new java.util.ArrayList<>());

        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));
        when(contactRepository.saveAndFlush(any(Contact.class))).thenReturn(contact);

        Contact result = contactService.updateContact(1, updatedData, user);

        assertEquals("Jane", result.getFirstName());
        verify(contactRepository).saveAndFlush(contact);
    }

    @Test
    void updateContact_shouldThrowWhenNotFoundForUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.updateContact(1, new ContactRequest(), user));
    }

    @Test
    void updateContact_nullRequest_shouldThrowAndNotMutate() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));

        assertThrows(IllegalArgumentException.class, () -> contactService.updateContact(1, null, user));

        assertEquals("John", contact.getFirstName());
    }

    @Test
    void updateContact_nullPhoneList_shouldPreserveExistingPhones() {
        Phone existingPhone = new Phone();
        existingPhone.setPhoneNumber("555-1111");
        contact.addPhone(existingPhone);

        ContactRequest updatedData = new ContactRequest();
        updatedData.setFirstName("Jane");
        updatedData.setEmails(new java.util.ArrayList<>());

        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));
        when(contactRepository.saveAndFlush(any(Contact.class))).thenReturn(contact);

        contactService.updateContact(1, updatedData, user);

        assertEquals(1, contact.getPhones().size());
        assertSame(existingPhone, contact.getPhones().get(0));
        assertEquals("555-1111", contact.getPhones().get(0).getPhoneNumber());
    }

    @Test
    void updateContact_nullEmailList_shouldPreserveExistingEmails() {
        Email existingEmail = new Email();
        existingEmail.setEmail("old@example.com");
        contact.addEmail(existingEmail);

        ContactRequest updatedData = new ContactRequest();
        updatedData.setFirstName("Jane");
        updatedData.setPhones(new java.util.ArrayList<>());

        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));
        when(contactRepository.saveAndFlush(any(Contact.class))).thenReturn(contact);

        contactService.updateContact(1, updatedData, user);

        assertEquals(1, contact.getEmails().size());
        assertSame(existingEmail, contact.getEmails().get(0));
        assertEquals("old@example.com", contact.getEmails().get(0).getEmail());
    }

    @Test
    void updateContact_partialUpdate_shouldPreserveOmittedAssociations() {
        Email existingEmail = new Email();
        existingEmail.setEmail("keep@example.com");
        Phone existingPhone = new Phone();
        existingPhone.setPhoneNumber("555-2222");
        contact.addEmail(existingEmail);
        contact.addPhone(existingPhone);

        // Update only the name; omit both lists entirely.
        ContactRequest updatedData = new ContactRequest();
        updatedData.setFirstName("Jane");

        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));
        when(contactRepository.saveAndFlush(any(Contact.class))).thenReturn(contact);

        Contact result = contactService.updateContact(1, updatedData, user);

        assertEquals("Jane", result.getFirstName());
        assertEquals(1, contact.getEmails().size());
        assertEquals(1, contact.getPhones().size());
        assertSame(existingEmail, contact.getEmails().get(0));
        assertSame(existingPhone, contact.getPhones().get(0));
    }

    @Test
    void updateContact_partialUpdate_shouldPreserveOmittedScalarFields() {
        contact.setTitle("CTO");

        // Update only the first name; omit lastName, title, and both lists.
        ContactRequest updatedData = new ContactRequest();
        updatedData.setFirstName("Jane");

        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));
        when(contactRepository.saveAndFlush(any(Contact.class))).thenReturn(contact);

        Contact result = contactService.updateContact(1, updatedData, user);

        assertEquals("Jane", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("CTO", result.getTitle());
    }

    @Test
    void updateContact_nullElementInPhones_shouldThrowAndNotMutate() {
        Phone existingPhone = new Phone();
        existingPhone.setPhoneNumber("555-3333");
        contact.addPhone(existingPhone);

        ContactRequest updatedData = new ContactRequest();
        updatedData.setPhones(java.util.Arrays.asList((PhoneRequest) null));

        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));

        assertThrows(IllegalArgumentException.class, () -> contactService.updateContact(1, updatedData, user));

        assertEquals(1, contact.getPhones().size());
        assertSame(existingPhone, contact.getPhones().get(0));
        assertEquals("John", contact.getFirstName());
    }
    @Test
    void getContactsPaginated_shouldThrowWhenSizeExceedsMax() {
        assertThrows(IllegalArgumentException.class,
                () -> contactService.getContactsPaginated(0, ContactService.MAX_PAGE_SIZE + 1, user));
        verifyNoInteractions(contactRepository);
    }

    @Test
    void getContactsPaginated_shouldThrowWhenPageNegativeOrSizeZero() {
        assertThrows(IllegalArgumentException.class, () -> contactService.getContactsPaginated(-1, 10, user));
        assertThrows(IllegalArgumentException.class, () -> contactService.getContactsPaginated(0, 0, user));
    }

    @Test
    void getContactsPaginated_shouldPassAscendingIdSortToRepository() {
        // Capture the Pageable handed to the repository and assert it carries an
        // ascending id sort so the database always returns contacts in a stable order.
        org.mockito.ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        when(contactRepository.findByUser(eq(user), captor.capture()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        contactService.getContactsPaginated(0, 10, user);

        org.springframework.data.domain.Sort sort = captor.getValue().getSort();
        org.springframework.data.domain.Sort.Order order = sort.getOrderFor("id");
        assertNotNull(order, "Pageable must include a sort on the id field");
        assertTrue(order.isAscending(), "Sort on id must be ascending");
    }

    @Test
    void getContactsPaginated_consecutivePages_shouldNotDuplicateOrOmitContacts() {
        // Build four contacts with distinct ids simulating two full pages of size 2.
        Contact c1 = makeContact(1, "Alice");
        Contact c2 = makeContact(2, "Bob");
        Contact c3 = makeContact(3, "Carol");
        Contact c4 = makeContact(4, "Dave");

        org.springframework.data.domain.PageImpl<Contact> page0 =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(c1, c2),
                        org.springframework.data.domain.PageRequest.of(0, 2,
                                org.springframework.data.domain.Sort.by(
                                        org.springframework.data.domain.Sort.Direction.ASC, "id")),
                        4);
        org.springframework.data.domain.PageImpl<Contact> page1 =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(c3, c4),
                        org.springframework.data.domain.PageRequest.of(1, 2,
                                org.springframework.data.domain.Sort.by(
                                        org.springframework.data.domain.Sort.Direction.ASC, "id")),
                        4);

        when(contactRepository.findByUser(eq(user),
                org.mockito.ArgumentMatchers.argThat(p -> p.getPageNumber() == 0)))
                .thenReturn(page0);
        when(contactRepository.findByUser(eq(user),
                org.mockito.ArgumentMatchers.argThat(p -> p.getPageNumber() == 1)))
                .thenReturn(page1);

        java.util.List<Contact> first  = contactService.getContactsPaginated(0, 2, user).getContent();
        java.util.List<Contact> second = contactService.getContactsPaginated(1, 2, user).getContent();

        java.util.Set<Integer> firstIds  = new java.util.HashSet<>();
        java.util.Set<Integer> secondIds = new java.util.HashSet<>();
        first.forEach(c  -> firstIds.add(c.getId()));
        second.forEach(c -> secondIds.add(c.getId()));

        // No contact appears on both pages.
        firstIds.retainAll(secondIds);
        assertTrue(firstIds.isEmpty(), "Consecutive pages must not share any contact ids");

        // Together they cover all four contacts.
        java.util.Set<Integer> all = new java.util.HashSet<>();
        first.forEach(c  -> all.add(c.getId()));
        second.forEach(c -> all.add(c.getId()));
        assertEquals(java.util.Set.of(1, 2, 3, 4), all, "All contacts must appear exactly once across consecutive pages");
    }

    private Contact makeContact(int id, String firstName) {
        Contact c = new Contact();
        c.setId(id);
        c.setFirstName(firstName);
        c.setUser(user);
        return c;
    }
}
