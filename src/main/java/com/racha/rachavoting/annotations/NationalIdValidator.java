package com.racha.rachavoting.annotations;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.racha.rachavoting.annotations.interfaces.NationalIdNotRegistered;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NationalIdValidator implements ConstraintValidator<NationalIdNotRegistered, String> {

    @Override
    public boolean isValid(String nationalId, ConstraintValidatorContext context) {
        if (nationalId == null || nationalId.isEmpty()) {
            return false; // Null or empty values are not validated
        }

        return validate(nationalId);
    }

    private static boolean validate(String idNumber) {
        try {
            Pattern pattern = Pattern.compile("[0-9]{13}");
            Matcher matcher = pattern.matcher(idNumber);

            if (!matcher.matches()) {
                return false;
            }

            if (!validateDate(idNumber.substring(0, 6))) {
                return false;
            }
            int lastNumber = Integer.parseInt(String.valueOf(idNumber.charAt(idNumber.length() - 1)));
            String numberSection = idNumber.substring(0, idNumber.length() - 1);

            return lastNumber == generateLuhnDigit(numberSection);
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean validateDate(String date) {
        int year = Integer.parseInt(date.substring(0, 2));
        int month = Integer.parseInt(date.substring(2, 4));

        if (month < 1 || month > 12) {
            return false;
        }

        int day = Integer.parseInt(date.substring(4, 6));
        Calendar myCal = new GregorianCalendar(year, month, day);
        int maxDays = myCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (day < 1 || day > maxDays) {
            return false;
        }

        return true;
    }

    private static int generateLuhnDigit(String input) {
        int total = 0;
        int count = 0;
        for (int i = 0; i < input.length(); i++) {
            int multiple = (count % 2) + 1;
            count++;
            int temp = multiple * Integer.parseInt(String.valueOf(input.charAt(i)));
            temp = (int) Math.floor(temp / 10) + (temp % 10);
            total += temp;
        }

        total = (total * 9) % 10;

        return total;
    }
}
