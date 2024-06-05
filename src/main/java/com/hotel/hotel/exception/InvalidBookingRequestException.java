package com.hotel.hotel.exception;

public class InvalidBookingRequestException  extends  RuntimeException{
    public InvalidBookingRequestException(String message) {
        super(message);
    }
}
