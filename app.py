"""Streamlit entry point for Personal AI Library Manager."""

from __future__ import annotations

import streamlit as st

from config import APP_NAME
from database import SessionLocal, initialise_database
from pages import add_book, catalog, dashboard, delete_book, edit_book, loans, reports, search_books, settings, view_books, wishlist

st.set_page_config(page_title=APP_NAME, page_icon="books", layout="wide")
initialise_database()

PAGES = {
    "Dashboard": dashboard,
    "Add Book": add_book,
    "View Books": view_books,
    "Search Books": search_books,
    "Edit Book": edit_book,
    "Delete Book": delete_book,
    "Reports": reports,
    "Tags and Collections": catalog,
    "Wishlist": wishlist,
    "Borrowed/Lent Books": loans,
    "Settings and Backup": settings,
}


def main() -> None:
    """Run the selected Phase 1 screen with a scoped database session."""
    st.sidebar.title(APP_NAME)
    selected_page = st.sidebar.radio("Navigation", list(PAGES), key="navigation")
    st.html(
        """
        <style>
        .library-masthead {
            background: linear-gradient(115deg, #14532d 0%, #0f766e 100%);
            border-left: 6px solid #facc15;
            box-sizing: border-box;
            display: flex;
            align-items: center;
            gap: 1rem;
            left: 21rem;
            padding: 1.25rem 1.5rem;
            position: fixed;
            right: 1.25rem;
            top: 3.25rem;
            z-index: 1000;
            box-shadow: 0 6px 14px rgba(20, 83, 45, 0.28);
        }
        .library-masthead-spacer { height: 13rem; }
        .library-mark {
            align-items: center;
            background: #facc15;
            border: 2px solid #ecfccb;
            box-shadow: inset -7px 0 0 #ca8a04;
            color: #14532d;
            display: flex;
            flex: 0 0 62px;
            font-family: Georgia, "Times New Roman", serif;
            font-size: 1.35rem;
            font-weight: 700;
            height: 62px;
            justify-content: center;
            letter-spacing: 0;
            line-height: 1;
            position: relative;
            width: 62px;
        }
        .library-mark::before,
        .library-mark::after {
            background: #14532d;
            content: "";
            height: 2px;
            left: 12px;
            position: absolute;
            width: 14px;
        }
        .library-mark::before { top: 12px; }
        .library-mark::after { bottom: 12px; }
        .library-mark span { position: relative; z-index: 1; }
        .library-masthead-copy { min-width: 0; }
        .library-masthead h1 {
            color: #ffffff;
            font-family: Georgia, "Times New Roman", serif;
            font-size: 2rem;
            font-weight: 700;
            letter-spacing: 0;
            line-height: 1.2;
            margin: 0;
        }
        .library-masthead p {
            color: #ecfccb;
            font-size: 0.95rem;
            letter-spacing: 0;
            line-height: 1.5;
            margin: 0.45rem 0 0;
        }
        .library-contact-row {
            align-items: center;
            display: flex;
            flex-wrap: wrap;
            gap: 0.4rem 1rem;
            margin-top: 0.45rem;
        }
        .library-contact-row p { margin: 0; }
        .library-contact-item {
            align-items: center;
            color: #ffffff;
            display: inline-flex;
            font-size: 0.95rem;
            font-weight: 600;
            gap: 0.35rem;
        }
        .library-contact-icon {
            color: #facc15;
            font-size: 1.05rem;
            line-height: 1;
        }
        .library-mobile-icon {
            border: 2px solid #ffffff;
            border-radius: 2px;
            box-sizing: border-box;
            display: inline-block;
            height: 1.05rem;
            position: relative;
            width: 0.66rem;
        }
        .library-mobile-icon::after {
            background: #facc15;
            border-radius: 50%;
            bottom: 1px;
            content: "";
            height: 2px;
            left: 50%;
            position: absolute;
            transform: translateX(-50%);
            width: 2px;
        }
        @media (max-width: 640px) {
            .library-masthead {
                align-items: flex-start;
                left: 0.5rem;
                padding: 1rem;
                right: 0.5rem;
                top: 3rem;
            }
            .library-masthead-spacer { height: 13.5rem; }
            .library-mark { flex-basis: 48px; height: 48px; width: 48px; }
            .library-masthead h1 { font-size: 1.5rem; }
        }
        </style>
        <section class="library-masthead">
            <div class="library-mark" aria-label="Venkatesh Gowda library monogram"><span>VG</span></div>
            <div class="library-masthead-copy">
                <h1>Venkatesh Gowdas Personal Library</h1>
                <p>Sharanya Milind Nilaya, Farm House, D. Hosahalli, Devanagondi Post, Bangalore 560117</p>
                <div class="library-contact-row">
                    <span class="library-contact-item"><span class="library-mobile-icon" aria-hidden="true"></span>9740819001 / 9740199339</span>
                    <span class="library-contact-item"><span class="library-contact-icon" aria-hidden="true">&#9993;</span>venkus@gmail.com</span>
                </div>
            </div>
        </section>
        <div class="library-masthead-spacer" aria-hidden="true"></div>
        """
    )
    with SessionLocal() as session:
        PAGES[selected_page].render(session)


if __name__ == "__main__":
    main()
