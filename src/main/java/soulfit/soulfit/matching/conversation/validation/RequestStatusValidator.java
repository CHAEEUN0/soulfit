package soulfit.soulfit.matching.conversation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import soulfit.soulfit.matching.conversation.domain.RequestStatus;

import java.util.Arrays;

public class RequestStatusValidator implements ConstraintValidator<ValidRequestStatus, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        // ACCEPTED, REJECTED 만 허용
        return Arrays.stream(RequestStatus.values())
                     .anyMatch(status -> status.name().equals(value.toUpperCase()) &&
                                         (status == RequestStatus.ACCEPTED || status == RequestStatus.REJECTED));
    }
}
