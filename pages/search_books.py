"""Catalogue search page with normal and optional fuzzy matching."""

import pandas as pd
import streamlit as st
from sqlalchemy.orm import Session

from services.book_service import BookService


SEARCH_FIELDS = (
    "All fields",
    "Title",
    "Author",
    "Category",
    "Publisher",
    "ISBN",
    "Tags",
    "Collections",
    "Notes",
    "Personal review",
)


def search_values(book: object, field: str) -> tuple[object, ...]:
    """Return the selected searchable values for a book."""
    values = {
        "Title": (book.book_name,),
        "Author": (book.author,),
        "Category": (book.category,),
        "Publisher": (book.publisher,),
        "ISBN": (book.isbn,),
        "Tags": tuple(tag.name for tag in book.tags),
        "Collections": tuple(collection.name for collection in book.collections),
        "Notes": (book.notes,),
        "Personal review": (book.personal_review,),
    }
    if field == "All fields":
        return tuple(value for field_values in values.values() for value in field_values)
    return values[field]


def render(session: Session) -> None:
    """Search books with normal modes or an optional ranked fuzzy match."""
    st.header("Search Books")
    service = BookService(session)
    filter_column, query_column = st.columns((1, 2))
    with filter_column:
        search_field = st.selectbox("Search in", SEARCH_FIELDS)
    with query_column:
        query = st.text_input(
            "Search text",
            placeholder="Enter text to search",
            key="search_query",
        )
    if not query.strip():
        st.info("Enter a search term to find books.")
        return
    fuzzy_enabled = st.toggle("Enable fuzzy search", value=False, help="Find similar spelling and typing variants.")
    if fuzzy_enabled:
        threshold = st.slider("Similarity threshold", min_value=0, max_value=100, value=70, step=5)
        results = service.fuzzy_search_books(query, threshold)
        if search_field != "All fields":
            normalized_query = query.strip().casefold()
            results = [
                result
                for result in results
                if any(normalized_query in str(value or "").casefold() for value in search_values(result.book, search_field))
            ]
        if not results:
            st.warning("No similar books meet this threshold.")
            return
        st.dataframe(
            pd.DataFrame(
                [
                    {
                        "Match score": round(result.score, 1),
                        "ID": result.book.id,
                        "Title": result.book.book_name,
                        "Author": result.book.author,
                        "Category": result.book.category or "-",
                    }
                    for result in results
                ]
            ),
            hide_index=True,
            use_container_width=True,
        )
        return

    match_mode = st.selectbox("Normal match", ("Contains", "Starts with", "Exact"))
    normalized_query = query.strip().casefold()
    books = service.search_books(query)
    if search_field != "All fields" or match_mode != "Contains":
        def matches(book: object) -> bool:
            normalized_values = [str(value or "").casefold() for value in search_values(book, search_field)]
            if match_mode == "Starts with":
                return any(value.startswith(normalized_query) for value in normalized_values)
            if match_mode == "Exact":
                return any(value == normalized_query for value in normalized_values)
            return any(normalized_query in value for value in normalized_values)

        books = [book for book in books if matches(book)]
    if not books:
        st.warning("No matching books found.")
        return
    st.dataframe(
        pd.DataFrame(
            [{"ID": book.id, "Title": book.book_name, "Author": book.author, "Category": book.category or "-"} for book in books]
        ),
        hide_index=True,
        use_container_width=True,
    )
