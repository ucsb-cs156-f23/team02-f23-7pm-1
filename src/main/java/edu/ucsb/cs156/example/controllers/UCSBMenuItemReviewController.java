package edu.ucsb.cs156.example.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.ucsb.cs156.example.entities.UCSBMenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBMenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "UCSBMenuItemReview")
@RequestMapping("/api/ucsbmenuitemreview")
@RestController
@Slf4j
public class UCSBMenuItemReviewController extends ApiController {

    @Autowired
    UCSBMenuItemReviewRepository ucsbMenuItemReviewRepository;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBMenuItemReview poUcsbMenuItemReview(
            @Parameter(name = "itemId", description = "Id for item being reviewed") @RequestParam long itemId,
            @Parameter(name = "stars") @RequestParam Integer stars,
            @Parameter(name = "reviewerEmail", description = "Email address of person who submitted review") @RequestParam String reviewerEmail,
            @Parameter(name = "dateReviewed", description = "Date of review in iso format, e.g. YYYY-mm-ddTHH:MM:SS") @RequestParam LocalDateTime dateReviewed,
            @Parameter(name = "comments") @RequestParam String comments)
            throws JsonProcessingException {

        UCSBMenuItemReview ucsbMenuItemReview = new UCSBMenuItemReview();
        ucsbMenuItemReview.setItemId(itemId);
        ucsbMenuItemReview.setStars(stars);
        ucsbMenuItemReview.setReviewerEmail(reviewerEmail);
        ucsbMenuItemReview.setDateReviewed(dateReviewed);
        ucsbMenuItemReview.setComments(comments);

        UCSBMenuItemReview savedUcsbMenuItemReview = ucsbMenuItemReviewRepository.save(ucsbMenuItemReview);

        return savedUcsbMenuItemReview;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBMenuItemReview> allUCSBMenuItemReviews() {
        Iterable<UCSBMenuItemReview> reviews = ucsbMenuItemReviewRepository.findAll();
        return reviews;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBMenuItemReview getById(
            @Parameter(name = "id") @RequestParam Long id) {
        UCSBMenuItemReview ucsbMenuItemReview = ucsbMenuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBMenuItemReview.class, id));

        return ucsbMenuItemReview;
    }
}
