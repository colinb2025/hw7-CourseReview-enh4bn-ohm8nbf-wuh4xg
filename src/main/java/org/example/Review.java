package org.example;

public class Review {
    private String reviewText;
    private int rating;

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if(rating>5||rating<1){
            throw new IllegalArgumentException("ratings must be between 1 and 5");
        }
        this.rating = rating;
    }
}
