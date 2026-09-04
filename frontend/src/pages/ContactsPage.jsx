import { useEffect, useRef, useState } from "react";
import { getContactsPage, searchContacts } from "../api/contacts";
import Alert from "../components/Alert";
import ContactFormModal from "../components/ContactFormModal";
import DeleteContactModal from "../components/DeleteContactModal";
import { getApiError } from "../lib/errors";

const PAGE_SIZE = 10;
const SEARCH_DELAY = 300;

function ContactRow({ contact, onEdit, onDelete }) {
  const fullName =
    [contact.firstName, contact.lastName].filter(Boolean).join(" ") ||
    "Unnamed contact";
  const emails = contact.emails ?? [];
  const phones = contact.phones ?? [];
  const hasDetails = emails.length > 0 || phones.length > 0;

  return (
    <li className="contact-row">
      <div className="contact-row-main">
        <span className="contact-name">{fullName}</span>
        {contact.title && (
          <span className="contact-title">{contact.title}</span>
        )}
      </div>
      {hasDetails && (
        <div className="contact-details">
          {emails.map((email) => (
            <span key={email.id} className="contact-detail">
              {email.label ? `${email.label}: ` : ""}
              {email.email}
            </span>
          ))}
          {phones.map((phone) => (
            <span key={phone.id} className="contact-detail">
              {phone.label ? `${phone.label}: ` : ""}
              {phone.phoneNumber}
            </span>
          ))}
        </div>
      )}
      <div className="contact-row-actions">
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          onClick={() => onEdit(contact)}
        >
          Edit
        </button>
        <button
          type="button"
          className="btn btn-ghost btn-sm contact-delete-btn"
          onClick={() => onDelete(contact)}
        >
          Delete
        </button>
      </div>
    </li>
  );
}

export default function ContactsPage() {
  const [pageData, setPageData] = useState(null);
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [page, setPage] = useState(0);
  const [query, setQuery] = useState("");
  const [debouncedQuery, setDebouncedQuery] = useState("");
  const [reloadToken, setReloadToken] = useState(0);

  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState("create");
  const [formContact, setFormContact] = useState(null);
  // Increments on every modal open so ContactFormModal remounts with a fresh
  // local state, discarding any previously dismissed, unsaved edits.
  const [formSession, setFormSession] = useState(0);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleteContact, setDeleteContact] = useState(null);
  const [banner, setBanner] = useState(null);
  const bannerTimerRef = useRef(null);

  const searching = debouncedQuery.trim().length > 0;

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQuery(query.trim());

      if (query.trim() && page !== 0) {
        setPage(0);
      }
    }, SEARCH_DELAY);

    return () => clearTimeout(timer);
  }, [query, page]);

  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setError(null);
      try {
        if (searching) {
          const data = await searchContacts(debouncedQuery);
          if (!active) return;
          setResults(data);
          setPageData(null);
        } else {
          const data = await getContactsPage(page, PAGE_SIZE);
          if (!active) return;

          // Guard against stale page numbers: if the server returned an empty
          // page but contacts still exist on earlier pages, clamp to the last
          // valid page. This happens when contacts on the current page are
          // deleted elsewhere. No second request is made — the response already
          // in hand is used to detect the condition.
          if (
            data.totalPages > 0 &&
            data.content.length === 0 &&
            page >= data.totalPages
          ) {
            setPage(data.totalPages - 1);
            return; // Effect will re-run with the corrected page.
          }

          setPageData(data);
          setResults(null);
        }
      } catch (err) {
        if (!active) return;
        setError(getApiError(err));
      } finally {
        if (active) setLoading(false);
      }
    }

    load();
    return () => {
      active = false;
    };
  }, [searching, debouncedQuery, page, reloadToken]);

  function handleQueryChange(event) {
    setQuery(event.target.value);
  }

  function handleRetry() {
    setReloadToken((value) => value + 1);
  }

  function goTo(nextPage) {
    if (nextPage < 0 || nextPage >= (pageData?.totalPages ?? 0)) return;
    setPage(nextPage);
  }

  function showBanner(message) {
    setBanner(message);
    if (bannerTimerRef.current) {
      window.clearTimeout(bannerTimerRef.current);
    }
    bannerTimerRef.current = window.setTimeout(() => setBanner(null), 4000);
  }

  useEffect(() => {
    return () => {
      if (bannerTimerRef.current) {
        window.clearTimeout(bannerTimerRef.current);
      }
    };
  }, []);

  function handleOpenCreate() {
    setFormMode("create");
    setFormContact(null);
    setFormSession((value) => value + 1);
    setFormOpen(true);
  }

  function handleEdit(contact) {
    setFormMode("edit");
    setFormContact(contact);
    setFormSession((value) => value + 1);
    setFormOpen(true);
  }

  function handleDeleteClick(contact) {
    setDeleteContact(contact);
    setDeleteOpen(true);
  }

  function handleFormClosed() {
    setFormOpen(false);
  }

  function handleFormSaved() {
    setFormOpen(false);
    setReloadToken((value) => value + 1);
    showBanner("Contact saved.");
  }

  function handleDeleteClosed() {
    setDeleteOpen(false);
  }

  function handleDeleteConfirmed() {
    setDeleteOpen(false);
    setDeleteContact(null);
    setPage(0);
    setReloadToken((value) => value + 1);
    showBanner("Contact deleted.");
  }

  const contacts = searching ? (results ?? []) : (pageData?.content ?? []);
  const hasContacts = contacts.length > 0;
  const currentPage = pageData?.totalPages > 0 ? pageData.number + 1 : 0;
  const isFirst = pageData?.first ?? true;
  const isLast = pageData?.last ?? true;

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Contacts</h1>
          <p className="page-desc">Browse all your saved contacts.</p>
        </div>
        {pageData && !searching && (
          <span className="badge">
            {pageData.totalElements}{" "}
            {pageData.totalElements === 1 ? "contact" : "contacts"}
          </span>
        )}
      </div>

      <div className="contacts-toolbar">
        <label className="search-field">
          <span className="sr-only">Search contacts by name</span>
          <input
            type="search"
            className="search-input"
            placeholder="Search by first or last name…"
            value={query}
            onChange={handleQueryChange}
          />
        </label>
        <button type="button" className="btn btn-primary" onClick={handleOpenCreate}>
          New contact
        </button>
      </div>

      {banner && (
        <div className="banner-wrap">
          <Alert variant="success">{banner}</Alert>
        </div>
      )}

      {error && (
        <div className="banner-wrap">
          <Alert variant="error">{error}</Alert>
          <button
            type="button"
            className="btn btn-ghost retry-btn"
            onClick={handleRetry}
          >
            Try again
          </button>
        </div>
      )}

      {loading ? (
        <div className="loading-state" role="status">
          <span className="spinner" aria-hidden="true" />
          <span>{searching ? "Searching…" : "Loading contacts…"}</span>
        </div>
      ) : error ? null : hasContacts ? (
        <>
          <ul className="card contact-list">
            {contacts.map((contact) => (
              <ContactRow
                key={contact.id}
                contact={contact}
                onEdit={handleEdit}
                onDelete={handleDeleteClick}
              />
            ))}
          </ul>

          {!searching && (
            <nav className="pagination" aria-label="Contact list pagination">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => goTo(page - 1)}
                disabled={isFirst || pageData.totalPages <= 1}
              >
                Previous
              </button>
              <span className="pagination-status">
                Page {currentPage} of {pageData.totalPages}
              </span>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => goTo(page + 1)}
                disabled={isLast || pageData.totalPages <= 1}
              >
                Next
              </button>
            </nav>
          )}
        </>
      ) : searching ? (
        <div className="empty-state">
          <span className="empty-state-icon" aria-hidden="true">
            ?
          </span>
          <h2 className="empty-state-title">No matches</h2>
          <p className="empty-state-text">
            No contacts matched “{debouncedQuery}”. Try a different name.
          </p>
        </div>
      ) : (
        <div className="empty-state">
          <span className="empty-state-icon" aria-hidden="true">
            +
          </span>
          <h2 className="empty-state-title">No contacts yet</h2>
          <p className="empty-state-text">
            Add your first contact with the “New contact” button above.
          </p>
        </div>
      )}

      <ContactFormModal
        key={formSession}
        open={formOpen}
        mode={formMode}
        contact={formContact}
        onClose={handleFormClosed}
        onSaved={handleFormSaved}
      />
      <DeleteContactModal
        open={deleteOpen}
        contact={deleteContact}
        onClose={handleDeleteClosed}
        onDeleted={handleDeleteConfirmed}
      />
    </>
  );
}
