package com.library.lms.service;

import com.library.lms.entity.IssueStatus;
import com.library.lms.entity.Role;
import com.library.lms.repository.BookIssueRepository;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private BookIssueRepository bookIssueRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FineService fineService;

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalBooks", bookRepository.count());
        summary.put("issuedBooks", bookIssueRepository.countByStatus(IssueStatus.ISSUED) + bookIssueRepository.countByStatus(IssueStatus.OVERDUE));
        summary.put("returnedBooks", bookIssueRepository.countByStatus(IssueStatus.RETURNED));
        summary.put("overdueBooks", bookIssueRepository.countOverdue(LocalDate.now()));
        summary.put("totalStudents", userRepository.countByRole(Role.STUDENT));
        summary.put("totalFineCollected", fineService.getTotalCollected());
        return summary;
    }

    public List<Map<String, Object>> getMostBorrowedBooks() {
        return objectArraysToMaps(bookIssueRepository.findMostBorrowedBooks(), "title", "count");
    }

    public List<Map<String, Object>> getMostActiveStudents() {
        return objectArraysToMaps(bookIssueRepository.findMostActiveStudents(), "name", "count");
    }

    public List<Map<String, Object>> getCategoryWiseBorrowing() {
        return objectArraysToMaps(bookIssueRepository.findCategoryWiseBorrowing(), "category", "count");
    }

    public List<Map<String, Object>> getDepartmentWiseBorrowing() {
        return objectArraysToMaps(bookIssueRepository.findDepartmentWiseBorrowing(), "department", "count");
    }

    private List<Map<String, Object>> objectArraysToMaps(List<Object[]> rows, String labelKey, String countKey) {
        return rows.stream().map(row -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(labelKey, row[0]);
            map.put(countKey, row[1]);
            return map;
        }).collect(Collectors.toList());
    }
}
