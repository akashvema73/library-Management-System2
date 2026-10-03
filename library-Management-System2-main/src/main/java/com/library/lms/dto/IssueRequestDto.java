package com.library.lms.dto;

import jakarta.validation.constraints.NotNull;

public class IssueRequestDto {

    @NotNull
    private Long bookId;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}
