// package com.longrich.smartgestion.service;

// import java.awt.Desktop;
// import java.awt.print.PrinterException;
// import java.awt.print.PrinterJob;
// import java.io.File;
// import java.io.IOException;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;

// import javax.print.PrintService;
// import javax.print.PrintServiceLookup;

// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.apache.pdfbox.pdmodel.PDPage;
// import org.apache.pdfbox.pdmodel.PDPageContentStream;
// import org.apache.pdfbox.pdmodel.common.PDRectangle;
// import org.apache.pdfbox.pdmodel.font.PDType1Font;
// import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
// import org.apache.pdfbox.printing.PDFPageable;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;

// @Service
// public class PrinterService {

//     private static final Logger logger = LoggerFactory.getLogger(PrinterService.class);
//     private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//     private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

//     // Format A6 (105mm x 148mm)
//     private static final PDRectangle A6_FORMAT = new PDRectangle(297.6f, 419.6f);

//     public boolean printPrescription(Prescription prescription) {
//         try {
//             File tempFile = createPrescriptionPDF(prescription);
//             if (tempFile == null) {
//                 logger.error("Failed to create PDF for prescription: {}", prescription.getPrescriptionNumber());
//                 return false;
//             }

//             File pdfDirectory = new File("./pdf_archives/");
//             if (!pdfDirectory.exists()) {
//                 pdfDirectory.mkdirs();
//             }
//             File savedCopy = new File(pdfDirectory, prescription.getPrescriptionNumber() + ".pdf");

//             java.nio.file.Files.copy(tempFile.toPath(), savedCopy.toPath(),
//                     java.nio.file.StandardCopyOption.REPLACE_EXISTING);

//             prescription.setPdfPath(savedCopy.getAbsolutePath());

//             logger.info("PDF sauvegardé dans : {}", savedCopy.getAbsolutePath());

//             PDDocument document = null;
//             try {
//                 document = PDDocument.load(tempFile);

//                 PrinterJob job = PrinterJob.getPrinterJob();
//                 job.setPageable(new PDFPageable(document));

//                 PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
//                 if (defaultPrintService != null) {
//                     job.setPrintService(defaultPrintService);
//                     job.print();
//                     logger.info("Prescription printed to default printer: {}", prescription.getPrescriptionNumber());
//                     return true;
//                 } else {
//                     if (job.printDialog()) {
//                         job.print();
//                         logger.info("Prescription printed via dialog: {}", prescription.getPrescriptionNumber());
//                         return true;
//                     } else {
//                         logger.info("Print dialog canceled for prescription: {}", prescription.getPrescriptionNumber());
//                         return false;
//                     }
//                 }
//             } finally {
//                 if (document != null) {
//                     document.close();
//                 }
//                 tempFile.delete();
//             }
//         } catch (IOException | PrinterException e) {
//             logger.error("Error printing prescription: {}", prescription.getPrescriptionNumber(), e);
//             return false;
//         }
//     }

//     public void openPrescriptionPDF(Prescription prescription) {
//         File pdfFile = createPrescriptionPDF(prescription);
//         if (pdfFile != null) {
//             try {
//                 Desktop.getDesktop().open(pdfFile);
//             } catch (IOException e) {
//                 logger.error("Impossible d'ouvrir le PDF", e);
//             }
//         }
//     }

//     public File createPrescriptionPDF(Prescription prescription) {
//         PDDocument document = new PDDocument();
//         File tempFile = null;

//         try {
//             float margin = 20;
//             float fontSize = 8;
//             float titleFontSize = 12;
//             float lineHeight = 12;
//             float maxTextWidth;

//             PDPage firstPage = new PDPage(A6_FORMAT);
//             document.addPage(firstPage);

//             PDPage currentPage = firstPage;
//             float yPosition = currentPage.getMediaBox().getHeight() - margin;
//             float width = currentPage.getMediaBox().getWidth() - 2 * margin;
//             maxTextWidth = width - margin - 15;

//             PDPageContentStream contentStream = null;

//             List<PDPageContentStream> contentStreams = new ArrayList<>();

//             try {
//                 contentStream = new PDPageContentStream(document, currentPage);
//                 contentStreams.add(contentStream);

//                 try (java.io.InputStream logoStream = getClass().getResourceAsStream("/images/logo.png")) {
//                     if (logoStream != null) {
//                         PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoStream.readAllBytes(),
//                                 "logo");
//                         float imageWidth = 40;
//                         float imageHeight = imageWidth * logo.getHeight() / logo.getWidth();
//                         contentStream.drawImage(logo, margin + width - imageWidth,
//                                 currentPage.getMediaBox().getHeight() - margin - imageHeight, imageWidth, imageHeight);
//                     }
//                 }

//                 // Title
//                 contentStream.beginText();
//                 contentStream.setFont(PDType1Font.HELVETICA_BOLD, titleFontSize);
//                 contentStream.newLineAtOffset(margin, yPosition);
//                 contentStream.showText("ORDONNANCE");
//                 contentStream.endText();
//                 yPosition -= lineHeight * 1.5f;

//                 // Doctor information
//                 contentStream.beginText();
//                 contentStream.setFont(PDType1Font.HELVETICA, fontSize);
//                 contentStream.newLineAtOffset(margin, yPosition);
//                 contentStream.showText("Médecin: " + prescription.getUser().getFullName());
//                 contentStream.endText();
//                 yPosition -= lineHeight * 1.5f;

//                 // Prescription number
//                 contentStream.beginText();
//                 contentStream.setFont(PDType1Font.HELVETICA, fontSize);
//                 contentStream.newLineAtOffset(margin, yPosition);
//                 contentStream.showText("N° d'ordonnance: " + prescription.getPrescriptionNumber());
//                 contentStream.endText();
//                 yPosition -= lineHeight;

//                 // Date et heure
//                 LocalDateTime now = LocalDateTime.now();
//                 contentStream.beginText();
//                 contentStream.setFont(PDType1Font.HELVETICA, fontSize);
//                 contentStream.newLineAtOffset(margin, yPosition);
//                 contentStream.showText("Date: " + prescription.getPrescriptionDate().format(DATE_FORMATTER) +
//                         " à " + now.format(TIME_FORMATTER));
//                 contentStream.endText();
//                 yPosition -= lineHeight * 1.5f;

//                 // Patient information
//                 contentStream.beginText();
//                 contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                 contentStream.newLineAtOffset(margin, yPosition);
//                 contentStream.showText("Patient:");
//                 contentStream.endText();
//                 yPosition -= lineHeight;

//                 contentStream.beginText();
//                 contentStream.setFont(PDType1Font.HELVETICA, fontSize);
//                 contentStream.newLineAtOffset(margin + 10, yPosition);
//                 contentStream.showText("Nom: " + prescription.getPatient().getFullName());
//                 contentStream.endText();
//                 yPosition -= lineHeight;

//                 if (prescription.getPatient().getAge() != null) {
//                     contentStream.beginText();
//                     contentStream.setFont(PDType1Font.HELVETICA, fontSize);
//                     contentStream.newLineAtOffset(margin + 10, yPosition);
//                     contentStream.showText("Âge: " + prescription.getPatient().getAge() + " ans");
//                     contentStream.endText();
//                     yPosition -= lineHeight;
//                 }

//                 if (prescription.getPatient().getDateOfBirth() != null) {
//                     contentStream.beginText();
//                     contentStream.setFont(PDType1Font.HELVETICA, fontSize);
//                     contentStream.newLineAtOffset(margin + 10, yPosition);
//                     contentStream.showText("Date de naissance: " +
//                             prescription.getPatient().getDateOfBirth().format(DATE_FORMATTER));
//                     contentStream.endText();
//                     yPosition -= lineHeight;
//                 }

//                 if (prescription.getPatient().getAddress() != null
//                         && !prescription.getPatient().getAddress().isEmpty()) {
//                     contentStream.beginText();
//                     contentStream.setFont(PDType1Font.HELVETICA, fontSize);
//                     contentStream.newLineAtOffset(margin + 10, yPosition);
//                     contentStream.showText("Adresse: " + prescription.getPatient().getAddress());
//                     contentStream.endText();
//                     yPosition -= lineHeight;
//                 }

//                 yPosition -= lineHeight * 0.5f;

//                 // Prescription items
//                 contentStream.beginText();
//                 contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                 contentStream.newLineAtOffset(margin, yPosition);
//                 contentStream.showText("Prescription:");
//                 contentStream.endText();
//                 yPosition -= lineHeight;

//                 // Ligne de séparation
//                 contentStream.setLineWidth(0.5f);
//                 contentStream.moveTo(margin, yPosition - lineHeight * 0.5f);
//                 contentStream.lineTo(width, yPosition - lineHeight * 0.5f);
//                 contentStream.stroke();
//                 yPosition -= lineHeight * 1.5f;

//                 double runningTotal = 0.0;
//                 for (PrescriptionItem item : prescription.getItems()) {
//                     if (yPosition < 70) {
//                         contentStream.close();

//                         PDPage newPage = new PDPage(A6_FORMAT);
//                         document.addPage(newPage);
//                         currentPage = newPage;

//                         yPosition = currentPage.getMediaBox().getHeight() - margin;

//                         contentStream = new PDPageContentStream(document, currentPage);
//                         contentStreams.add(contentStream);

//                         contentStream.beginText();
//                         contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                         contentStream.newLineAtOffset(margin, yPosition);
//                         contentStream.showText("Suite de l'ordonnance - " + prescription.getPrescriptionNumber());
//                         contentStream.endText();
//                         yPosition -= lineHeight * 2;
//                     }

//                     // Product name with quantity and cost
//                     contentStream.beginText();
//                     contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                     contentStream.newLineAtOffset(margin + 5, yPosition);
//                     double itemCost = item.getItemTotal();
//                     runningTotal += itemCost;
//                     contentStream.showText(String.format("• %s x%d - %.2f FCFA",
//                             item.getProduct().getName(),
//                             item.getQuantity(),
//                             itemCost));
//                     contentStream.endText();
//                     yPosition -= lineHeight;

//                     String posologyText = formatPosologie(item.getPosologie());
//                     float posologyWidth = PDType1Font.HELVETICA.getStringWidth(posologyText) / 1000 * (fontSize - 1);

//                     if (posologyWidth > maxTextWidth) {
//                         int maxCharsPerLine = (int) (maxTextWidth * 1000
//                                 / ((fontSize - 1) * PDType1Font.HELVETICA.getAverageFontWidth()));
//                         String[] chunks = splitTextToFitWidth(posologyText, maxCharsPerLine);

//                         for (String chunk : chunks) {
//                             contentStream.beginText();
//                             contentStream.setFont(PDType1Font.HELVETICA, fontSize - 1);
//                             contentStream.newLineAtOffset(margin + 15, yPosition);
//                             contentStream.showText(chunk);
//                             contentStream.endText();
//                             yPosition -= lineHeight;

//                             if (yPosition < 70 && chunk != chunks[chunks.length - 1]) {
//                                 contentStream.close();

//                                 PDPage newPage = new PDPage(A6_FORMAT);
//                                 document.addPage(newPage);
//                                 currentPage = newPage;

//                                 yPosition = currentPage.getMediaBox().getHeight() - margin;

//                                 contentStream = new PDPageContentStream(document, currentPage);
//                                 contentStreams.add(contentStream);

//                                 contentStream.beginText();
//                                 contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                                 contentStream.newLineAtOffset(margin, yPosition);
//                                 contentStream
//                                         .showText("Suite de l'ordonnance - " + prescription.getPrescriptionNumber());
//                                 contentStream.endText();
//                                 yPosition -= lineHeight * 2;
//                             }
//                         }
//                     } else {
//                         contentStream.beginText();
//                         contentStream.setFont(PDType1Font.HELVETICA, fontSize - 1);
//                         contentStream.newLineAtOffset(margin + 15, yPosition);
//                         contentStream.showText(posologyText);
//                         contentStream.endText();
//                         yPosition -= lineHeight;
//                     }
//                     if (item.getInstructions() != null && !item.getInstructions().isEmpty()) {
//                         String instrText = "Instructions: " + item.getInstructions();
//                         int maxCharsPerLine = (int) (maxTextWidth * 1000
//                                 / ((fontSize - 1) * PDType1Font.HELVETICA.getAverageFontWidth()));
//                         String[] chunks = splitTextToFitWidth(instrText, maxCharsPerLine);
//                         for (String chunk : chunks) {
//                             contentStream.beginText();
//                             contentStream.setFont(PDType1Font.HELVETICA, fontSize - 1);
//                             contentStream.newLineAtOffset(margin + 15, yPosition);
//                             contentStream.showText(chunk);
//                             contentStream.endText();
//                             yPosition -= lineHeight;

//                             if (yPosition < 70 && chunk != chunks[chunks.length - 1]) {
//                                 contentStream.close();

//                                 PDPage newPage = new PDPage(A6_FORMAT);
//                                 document.addPage(newPage);
//                                 currentPage = newPage;

//                                 yPosition = currentPage.getMediaBox().getHeight() - margin;

//                                 contentStream = new PDPageContentStream(document, currentPage);
//                                 contentStreams.add(contentStream);

//                                 contentStream.beginText();
//                                 contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                                 contentStream.newLineAtOffset(margin, yPosition);
//                                 contentStream
//                                         .showText("Suite de l'ordonnance - " + prescription.getPrescriptionNumber());
//                                 contentStream.endText();
//                                 yPosition -= lineHeight * 2;
//                             }
//                         }
//                     }
//                     yPosition -= lineHeight * 0.5f;
//                 }
//                 if (yPosition < 80) {
//                     contentStream.close();

//                     PDPage newPage = new PDPage(A6_FORMAT);
//                     document.addPage(newPage);
//                     currentPage = newPage;

//                     yPosition = currentPage.getMediaBox().getHeight() - margin;

//                     contentStream = new PDPageContentStream(document, currentPage);
//                     contentStreams.add(contentStream);

//                     contentStream.beginText();
//                     contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                     contentStream.newLineAtOffset(margin, yPosition);
//                     contentStream.showText("Suite de l'ordonnance - " + prescription.getPrescriptionNumber());
//                     contentStream.endText();
//                     yPosition -= lineHeight * 2;
//                 }

//                 contentStream.beginText();
//                 contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                 contentStream.newLineAtOffset(margin, yPosition);
//                 contentStream.showText(String.format("Total : %.2f FCFA", prescription.getTotalCost()));
//                 contentStream.endText();
//                 yPosition -= lineHeight * 1.5f;

//                 // Notes
//                 if (prescription.getNotes() != null && !prescription.getNotes().isEmpty()) {
//                     if (yPosition < 100) {
//                         contentStream.close();

//                         PDPage newPage = new PDPage(A6_FORMAT);
//                         document.addPage(newPage);
//                         currentPage = newPage;

//                         yPosition = currentPage.getMediaBox().getHeight() - margin;

//                         contentStream = new PDPageContentStream(document, currentPage);
//                         contentStreams.add(contentStream);
//                     }

//                     yPosition -= lineHeight * 0.5f;

//                     contentStream.setLineWidth(0.5f);
//                     contentStream.moveTo(margin, yPosition - lineHeight * 0.5f);
//                     contentStream.lineTo(width, yPosition - lineHeight * 0.5f);
//                     contentStream.stroke();
//                     yPosition -= lineHeight;

//                     contentStream.beginText();
//                     contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                     contentStream.newLineAtOffset(margin, yPosition);
//                     contentStream.showText("Notes:");
//                     contentStream.endText();
//                     yPosition -= lineHeight;

//                     String notes = prescription.getNotes();
//                     int maxCharsPerLine = (int) (maxTextWidth * 1000
//                             / ((fontSize - 1) * PDType1Font.HELVETICA.getAverageFontWidth()));
//                     String[] chunks = splitTextToFitWidth(notes, maxCharsPerLine);

//                     for (String chunk : chunks) {
//                         contentStream.beginText();
//                         contentStream.setFont(PDType1Font.HELVETICA, fontSize - 1);
//                         contentStream.newLineAtOffset(margin + 10, yPosition);
//                         contentStream.showText(chunk);
//                         contentStream.endText();
//                         yPosition -= lineHeight;

//                         if (yPosition < 70 && chunk != chunks[chunks.length - 1]) {
//                             contentStream.close();

//                             PDPage newPage = new PDPage(A6_FORMAT);
//                             document.addPage(newPage);
//                             currentPage = newPage;

//                             yPosition = currentPage.getMediaBox().getHeight() - margin;

//                             contentStream = new PDPageContentStream(document, currentPage);
//                             contentStreams.add(contentStream);

//                             contentStream.beginText();
//                             contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                             contentStream.newLineAtOffset(margin, yPosition);
//                             contentStream.showText("Suite de l'ordonnance - " + prescription.getPrescriptionNumber());
//                             contentStream.endText();
//                             yPosition -= lineHeight * 2;
//                         }
//                     }
//                 }

//                 // Signature
//                 if (yPosition < 70) {
//                     contentStream.close();

//                     PDPage newPage = new PDPage(A6_FORMAT);
//                     document.addPage(newPage);
//                     currentPage = newPage;

//                     contentStream = new PDPageContentStream(document, currentPage);
//                     contentStreams.add(contentStream);
//                 }

//                 yPosition = 50;
//                 contentStream.beginText();
//                 contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
//                 contentStream.newLineAtOffset(width - 80, yPosition);
//                 contentStream.showText("Signature:");
//                 contentStream.endText();

//                 contentStream.setLineWidth(0.5f);
//                 contentStream.moveTo(width - 80, yPosition - lineHeight * 0.5f);
//                 contentStream.lineTo(width - 10, yPosition - lineHeight * 0.5f);
//                 contentStream.stroke();
//             } finally {
//                 for (PDPageContentStream stream : contentStreams) {
//                     if (stream != null) {
//                         try {
//                             stream.close();
//                         } catch (IOException e) {
//                             logger.error("Erreur lors de la fermeture d'un content stream", e);
//                         }
//                     }
//                 }
//             }

//             tempFile = File.createTempFile("prescription_", ".pdf");
//             document.save(tempFile);
//             return tempFile;

//         } catch (IOException e) {
//             logger.error("Error creating PDF for prescription: {}", prescription.getPrescriptionNumber(), e);
//             return null;
//         } finally {
//             try {
//                 document.close();
//             } catch (IOException e) {
//                 logger.error("Error closing PDF document", e);
//             }
//         }
//     }

//     private String formatPosologie(Posologie posologie) {
//         if (posologie == null) {
//             return "";
//         }
//         StringBuilder sb = new StringBuilder();
//         sb.append("Matin ").append(posologie.getMorningDose())
//                 .append(", Midi ").append(posologie.getNoonDose())
//                 .append(", Soir ").append(posologie.getEveningDose());

//         if (posologie.getDurationDays() != null) {
//             sb.append(" pendant ").append(posologie.getDurationDays()).append(" jour(s)");
//         }

//         if (posologie.isTakeWithFood()) {
//             sb.append(" - Avec nourriture");
//         }
//         if (posologie.isTakeAfterFood()) {
//             sb.append(" - Après repas");
//         }
//         if (posologie.isTakeJointFood()) {
//             sb.append(" - Avec prise conjointe de nourriture");
//         }

//         if (posologie.getSpecialInstructions() != null && !posologie.getSpecialInstructions().isEmpty()) {
//             sb.append(" - ").append(posologie.getSpecialInstructions());
//         }

//         return sb.toString();
//     }

//     private String[] splitTextToFitWidth(String text, int maxCharsPerLine) {
//         if (text == null || text.isEmpty()) {
//             return new String[0];
//         }

//         int numLines = (int) Math.ceil((double) text.length() / maxCharsPerLine);
//         String[] result = new String[numLines];

//         for (int i = 0; i < numLines; i++) {
//             int startIdx = i * maxCharsPerLine;
//             int endIdx = Math.min(startIdx + maxCharsPerLine, text.length());

//             if (endIdx < text.length() && endIdx > startIdx) {
//                 int lastSpaceIdx = text.substring(startIdx, endIdx).lastIndexOf(' ');
//                 if (lastSpaceIdx > 0) {
//                     endIdx = startIdx + lastSpaceIdx + 1;
//                 }
//             }

//             result[i] = text.substring(startIdx, endIdx).trim();
//         }

//         return result;
//     }
// }