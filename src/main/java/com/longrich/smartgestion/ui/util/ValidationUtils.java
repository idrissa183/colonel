package com.longrich.smartgestion.ui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ValidationUtils {

    public static final Color ERROR_COLOR = new Color(231, 76, 60);
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    public static final Color DEFAULT_BORDER_COLOR = new Color(206, 212, 218);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color PARTIAL_COLOR = new Color(255, 193, 7); // Orange pour validation partielle

    /**
     * Enum representing the different states of validation
     */
    public enum ValidationState {
        NEUTRAL,   // Empty or initial state
        PARTIAL,   // Partially valid (could become valid)
        VALID,     // Completely valid
        INVALID    // Invalid and cannot become valid with current input
    }

    // Regex patterns for Burkina Faso context
    public static final String BURKINA_PHONE_REGEX = "^(\\+226[02567]\\d{7}|[02567]\\d{7})$";
    public static final String CNIB_REGEX = "^B\\d{8}$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String CODE_PARTENAIRE_REGEX = "^[A-Z]{2}\\d{8}$";
    public static final String CODE_STOCKISTE_REGEX = "^[A-Z]{2}\\d{4}$";
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9._-]{4,20}$";
    public static final String NAME_REGEX = "^[a-zA-ZÀ-ÿ\\s-']{2,50}$";

    // Compiled patterns for better performance
    private static final Pattern BURKINA_PHONE_PATTERN = Pattern.compile(BURKINA_PHONE_REGEX);
    private static final Pattern CNIB_PATTERN = Pattern.compile(CNIB_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern CODE_PARTENAIRE_PATTERN = Pattern.compile(CODE_PARTENAIRE_REGEX);
    private static final Pattern CODE_STOCKISTE_PATTERN = Pattern.compile(CODE_STOCKISTE_REGEX);
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    /**
     * Validates Burkina Faso phone number
     */
    public static boolean isValidBurkinaPhone(String phone) {
        return phone == null || phone.trim().isEmpty() || BURKINA_PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates CNIB number
     */
    public static boolean isValidCNIB(String cnib) {
        return cnib == null || cnib.trim().isEmpty() || CNIB_PATTERN.matcher(cnib.trim()).matches();
    }

    /**
     * Validates email address
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates partner code (ISO2 + 8 digits)
     */
    public static boolean isValidCodePartenaire(String code) {
        return code == null || code.trim().isEmpty() || CODE_PARTENAIRE_PATTERN.matcher(code.trim()).matches();
    }

    /**
     * Validates stockiste code (ISO2 + 4 digits)
     */
    public static boolean isValidCodeStockiste(String code) {
        return code != null && !code.trim().isEmpty() && CODE_STOCKISTE_PATTERN.matcher(code.trim()).matches();
    }

    /**
     * Validates username
     */
    public static boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty() && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Validates name (first name, last name)
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && NAME_PATTERN.matcher(name.trim()).matches();
    }

    // ===== PROGRESSIVE VALIDATION METHODS =====

    /**
     * Validates phone progressively as user types
     */
    public static ValidationState validateBurkinaPhoneProgressive(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ValidationState.NEUTRAL;
        }
        
        String trimmed = phone.trim();
        
        // Check if it matches the complete pattern
        if (BURKINA_PHONE_PATTERN.matcher(trimmed).matches()) {
            return ValidationState.VALID;
        }
        
        // Check if it could potentially be valid (progressive validation)
        if (isProgressivelyValidPhone(trimmed)) {
            return ValidationState.PARTIAL;
        }
        
        return ValidationState.INVALID;
    }

    /**
     * Validates CNIB progressively as user types
     */
    public static ValidationState validateCNIBProgressive(String cnib) {
        if (cnib == null || cnib.trim().isEmpty()) {
            return ValidationState.NEUTRAL;
        }
        
        String trimmed = cnib.trim();
        
        // Check if it matches the complete pattern
        if (CNIB_PATTERN.matcher(trimmed).matches()) {
            return ValidationState.VALID;
        }
        
        // Check if it could potentially be valid
        if (isProgressivelyValidCNIB(trimmed)) {
            return ValidationState.PARTIAL;
        }
        
        return ValidationState.INVALID;
    }

    /**
     * Validates code partenaire progressively as user types
     */
    public static ValidationState validateCodePartenaireProgressive(String code) {
        if (code == null || code.trim().isEmpty()) {
            return ValidationState.NEUTRAL;
        }
        
        String trimmed = code.trim();
        
        // Check if it matches the complete pattern
        if (CODE_PARTENAIRE_PATTERN.matcher(trimmed).matches()) {
            return ValidationState.VALID;
        }
        
        // Check if it could potentially be valid
        if (isProgressivelyValidCodePartenaire(trimmed)) {
            return ValidationState.PARTIAL;
        }
        
        return ValidationState.INVALID;
    }

    /**
     * Validates code stockiste progressively as user types
     */
    public static ValidationState validateCodeStockisteProgressive(String code) {
        if (code == null || code.trim().isEmpty()) {
            return ValidationState.NEUTRAL;
        }
        
        String trimmed = code.trim();
        
        // Check if it matches the complete pattern
        if (CODE_STOCKISTE_PATTERN.matcher(trimmed).matches()) {
            return ValidationState.VALID;
        }
        
        // Check if it could potentially be valid
        if (isProgressivelyValidCodeStockiste(trimmed)) {
            return ValidationState.PARTIAL;
        }
        
        return ValidationState.INVALID;
    }

    /**
     * Validates email progressively as user types
     */
    public static ValidationState validateEmailProgressive(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationState.NEUTRAL;
        }
        
        String trimmed = email.trim();
        
        // Check if it matches the complete pattern
        if (EMAIL_PATTERN.matcher(trimmed).matches()) {
            return ValidationState.VALID;
        }
        
        // Check if it could potentially be valid
        if (isProgressivelyValidEmail(trimmed)) {
            return ValidationState.PARTIAL;
        }
        
        return ValidationState.INVALID;
    }

    // ===== PROGRESSIVE VALIDATION HELPERS =====

    private static boolean isProgressivelyValidPhone(String phone) {
        // Valid starting patterns for Burkina phone
        if (phone.startsWith("+226")) {
            if (phone.length() <= 4) return true; // "+226"
            String afterCode = phone.substring(4);
            if (afterCode.length() > 8) return false; // Too long
            if (afterCode.isEmpty()) return true;
            char firstDigit = afterCode.charAt(0);
            if (firstDigit == '0' || firstDigit == '2' || firstDigit == '5' || firstDigit == '6' || firstDigit == '7') {
                return afterCode.matches("\\d*"); // Only digits after valid first digit
            }
            return false;
        } else if (phone.length() <= 8) {
            if (phone.isEmpty()) return true;
            char firstChar = phone.charAt(0);
            if (firstChar == '0' || firstChar == '2' || firstChar == '5' || firstChar == '6' || firstChar == '7') {
                return phone.matches("\\d*"); // Only digits
            }
            return false;
        }
        return false;
    }

    private static boolean isProgressivelyValidCNIB(String cnib) {
        if (cnib.length() > 9) return false; // Too long
        if (cnib.startsWith("B")) {
            String digits = cnib.substring(1);
            return digits.matches("\\d*") && digits.length() <= 8;
        } else if (cnib.length() == 1 && cnib.equals("B")) {
            return true;
        }
        return false;
    }

    private static boolean isProgressivelyValidCodePartenaire(String code) {
        if (code.length() > 10) return false; // Too long (2 letters + 8 digits)
        
        if (code.length() <= 2) {
            return code.matches("[A-Z]*"); // Only uppercase letters
        } else {
            String letters = code.substring(0, 2);
            String digits = code.substring(2);
            return letters.matches("[A-Z]{2}") && digits.matches("\\d*") && digits.length() <= 8;
        }
    }

    private static boolean isProgressivelyValidCodeStockiste(String code) {
        if (code.length() > 6) return false; // Too long (2 letters + 4 digits)
        
        if (code.length() <= 2) {
            return code.matches("[A-Z]*"); // Only uppercase letters
        } else {
            String letters = code.substring(0, 2);
            String digits = code.substring(2);
            return letters.matches("[A-Z]{2}") && digits.matches("\\d*") && digits.length() <= 4;
        }
    }

    private static boolean isProgressivelyValidEmail(String email) {
        // Basic progressive email validation
        if (email.contains("@")) {
            String[] parts = email.split("@");
            if (parts.length > 2) return false; // Too many @
            if (parts.length == 2) {
                return parts[0].length() > 0 && parts[1].matches("[a-zA-Z0-9.-]*");
            }
        }
        return email.matches("[a-zA-Z0-9.+_-]*");
    }

    /**
     * Checks if a field is required and not empty
     */
    public static boolean isRequired(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Creates a JLabel for displaying field errors
     */
    public static JLabel createErrorLabel() {
        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        errorLabel.setVisible(false);
        return errorLabel;
    }

    /**
     * Shows an error message on a field
     */
    public static void showFieldError(JLabel errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Hides the error message on a field
     */
    public static void hideFieldError(JLabel errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    /**
     * Sets error border on a component
     */
    public static void setErrorBorder(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ERROR_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)));
    }

    /**
     * Sets success border on a component
     */
    public static void setSuccessBorder(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUCCESS_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)));
    }

    /**
     * Sets default border on a component
     */
    public static void setDefaultBorder(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)));
    }

    /**
     * Sets partial border on a component (orange for in-progress validation)
     */
    public static void setPartialBorder(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PARTIAL_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)));
    }

    /**
     * Sets border based on validation state
     */
    public static void setBorderByState(JComponent component, ValidationState state) {
        switch (state) {
            case NEUTRAL:
                setDefaultBorder(component);
                break;
            case PARTIAL:
                setPartialBorder(component);
                break;
            case VALID:
                setSuccessBorder(component);
                break;
            case INVALID:
                setErrorBorder(component);
                break;
        }
    }

    /**
     * Adds real-time validation to a text field
     */
    public static void addFieldValidator(JTextField field, JLabel errorLabel, 
                                       String errorMessage, Predicate<String> validator) {
        addFieldValidator(field, errorLabel, errorMessage, validator, false);
    }

    /**
     * Adds real-time validation to a text field with required option
     */
    public static void addFieldValidator(JTextField field, JLabel errorLabel, 
                                       String errorMessage, Predicate<String> validator, 
                                       boolean required) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateField(); }
            public void removeUpdate(DocumentEvent e) { validateField(); }
            public void insertUpdate(DocumentEvent e) { validateField(); }

            private void validateField() {
                String text = field.getText();
                boolean isEmpty = text.trim().isEmpty();
                boolean isValid = validator.test(text);

                if (required && isEmpty) {
                    showFieldError(errorLabel, "Ce champ est obligatoire");
                    setErrorBorder(field);
                } else if (!isEmpty && !isValid) {
                    showFieldError(errorLabel, errorMessage);
                    setErrorBorder(field);
                } else if (!isEmpty && isValid) {
                    hideFieldError(errorLabel);
                    setSuccessBorder(field);
                } else {
                    hideFieldError(errorLabel);
                    setDefaultBorder(field);
                }
            }
        });
    }

    /**
     * Adds progressive real-time validation to a text field with specific validation function
     */
    public static void addProgressiveFieldValidator(JTextField field, JLabel errorLabel, 
                                                   String errorMessage, 
                                                   java.util.function.Function<String, ValidationState> progressiveValidator,
                                                   boolean required) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateField(); }
            public void removeUpdate(DocumentEvent e) { validateField(); }
            public void insertUpdate(DocumentEvent e) { validateField(); }

            private void validateField() {
                String text = field.getText();
                boolean isEmpty = text.trim().isEmpty();
                
                if (required && isEmpty) {
                    showFieldError(errorLabel, "Ce champ est obligatoire");
                    setErrorBorder(field);
                    return;
                }
                
                if (isEmpty) {
                    hideFieldError(errorLabel);
                    setDefaultBorder(field);
                    return;
                }
                
                ValidationState state = progressiveValidator.apply(text);
                setBorderByState(field, state);
                
                switch (state) {
                    case INVALID:
                        showFieldError(errorLabel, errorMessage);
                        break;
                    case PARTIAL:
                        showFieldError(errorLabel, "En cours de saisie...");
                        errorLabel.setForeground(PARTIAL_COLOR);
                        break;
                    case VALID:
                        hideFieldError(errorLabel);
                        break;
                    case NEUTRAL:
                    default:
                        hideFieldError(errorLabel);
                        break;
                }
            }
        });
    }

    // ===== CONVENIENCE METHODS FOR SPECIFIC FIELD TYPES =====

    /**
     * Adds progressive validation for Burkina phone number field
     */
    public static void addBurkinaPhoneValidator(JTextField field, JLabel errorLabel, boolean required) {
        addProgressiveFieldValidator(field, errorLabel, 
                ErrorMessages.INVALID_PHONE, 
                ValidationUtils::validateBurkinaPhoneProgressive, 
                required);
    }

    /**
     * Adds progressive validation for CNIB field
     */
    public static void addCNIBValidator(JTextField field, JLabel errorLabel, boolean required) {
        addProgressiveFieldValidator(field, errorLabel, 
                ErrorMessages.INVALID_CNIB, 
                ValidationUtils::validateCNIBProgressive, 
                required);
    }

    /**
     * Adds progressive validation for code partenaire field
     */
    public static void addCodePartenaireValidator(JTextField field, JLabel errorLabel, boolean required) {
        addProgressiveFieldValidator(field, errorLabel, 
                ErrorMessages.INVALID_CODE_PARTENAIRE, 
                ValidationUtils::validateCodePartenaireProgressive, 
                required);
    }

    /**
     * Adds progressive validation for code stockiste field
     */
    public static void addCodeStockisteValidator(JTextField field, JLabel errorLabel, boolean required) {
        addProgressiveFieldValidator(field, errorLabel, 
                ErrorMessages.INVALID_CODE_STOCKISTE, 
                ValidationUtils::validateCodeStockisteProgressive, 
                required);
    }

    /**
     * Adds progressive validation for email field
     */
    public static void addEmailValidator(JTextField field, JLabel errorLabel, boolean required) {
        addProgressiveFieldValidator(field, errorLabel, 
                ErrorMessages.INVALID_EMAIL, 
                ValidationUtils::validateEmailProgressive, 
                required);
    }

    /**
     * Validates all fields and returns true if all are valid
     */
    public static boolean validateAllFields(ValidationField... fields) {
        boolean allValid = true;
        
        for (ValidationField field : fields) {
            boolean isValid = field.validate();
            if (!isValid) {
                allValid = false;
            }
        }
        
        return allValid;
    }

    /**
     * Creates a text field with standard formatting
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        setDefaultBorder(field);
        return field;
    }

    /**
     * Creates a panel containing a field with its label and error label
     */
    public static JPanel createFieldPanel(String labelText, JComponent inputComponent, JLabel errorLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setOpaque(false);

        // Label du champ
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);

        panel.add(Box.createVerticalStrut(5));

        // Input avec gestion de la taille
        inputComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (inputComponent instanceof JTextField || inputComponent instanceof JComboBox) {
            inputComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, inputComponent.getPreferredSize().height));
        }
        panel.add(inputComponent);

        panel.add(Box.createVerticalStrut(3));

        // Label d'erreur
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(errorLabel);

        return panel;
    }

    /**
     * Represents a field that can be validated
     */
    public static class ValidationField {
        private final JTextField field;
        private final JLabel errorLabel;
        private final String errorMessage;
        private final Predicate<String> validator;
        private final boolean required;

        public ValidationField(JTextField field, JLabel errorLabel, String errorMessage, 
                             Predicate<String> validator, boolean required) {
            this.field = field;
            this.errorLabel = errorLabel;
            this.errorMessage = errorMessage;
            this.validator = validator;
            this.required = required;
        }

        public boolean validate() {
            String text = field.getText();
            boolean isEmpty = text.trim().isEmpty();
            boolean isValid = validator.test(text);

            if (required && isEmpty) {
                showFieldError(errorLabel, "Ce champ est obligatoire");
                setErrorBorder(field);
                return false;
            } else if (!isEmpty && !isValid) {
                showFieldError(errorLabel, errorMessage);
                setErrorBorder(field);
                return false;
            } else {
                hideFieldError(errorLabel);
                if (!isEmpty && isValid) {
                    setSuccessBorder(field);
                } else {
                    setDefaultBorder(field);
                }
                return true;
            }
        }

        public String getValue() {
            return field.getText().trim();
        }
    }

    /**
     * Error messages in French for common validations
     */
    public static class ErrorMessages {
        public static final String REQUIRED = "Ce champ est obligatoire";
        public static final String INVALID_EMAIL = "Format d'email invalide";
        public static final String INVALID_PHONE = "Numéro de téléphone burkinabè invalide";
        public static final String INVALID_CNIB = "Le numéro de CNIB doit respecter le format B suivi de 8 chiffres";
        public static final String INVALID_CODE_PARTENAIRE = "Le code partenaire doit respecter le format ISO2 suivi de 8 chiffres (ex: BF12345678)";
        public static final String INVALID_CODE_STOCKISTE = "Le code stockiste doit respecter le format ISO2 suivi de 4 chiffres (ex: BF1234)";
        public static final String INVALID_USERNAME = "Le nom d'utilisateur doit contenir 4-20 caractères alphanumériques";
        public static final String INVALID_NAME = "Le nom doit contenir entre 2 et 50 caractères (lettres, espaces, tirets)";
    }
}