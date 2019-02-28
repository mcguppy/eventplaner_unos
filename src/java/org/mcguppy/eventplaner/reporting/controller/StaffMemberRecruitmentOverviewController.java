package org.mcguppy.eventplaner.reporting.controller;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import org.mcguppy.eventplaner.jpa.controllers.LocationJpaController;
import org.mcguppy.eventplaner.jpa.controllers.ShiftJpaController;
import org.mcguppy.eventplaner.jpa.controllers.StaffMemberJpaController;
import org.mcguppy.eventplaner.jpa.entities.Location;
import org.mcguppy.eventplaner.jpa.entities.Shift;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
@Named
@RequestScoped
public class StaffMemberRecruitmentOverviewController {

    public StaffMemberRecruitmentOverviewController() {
        facesContext = FacesContext.getCurrentInstance();
        locationJpaController = (LocationJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "locationJpa");
        shiftJpaController = (ShiftJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "shiftJpa");
        staffMemberJpaController = (StaffMemberJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "staffMemberJpa");
        dateString = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        dateStringHuman = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
    }
    private LocationJpaController locationJpaController;
    private ShiftJpaController shiftJpaController;
    private StaffMemberJpaController staffMemberJpaController;
    private FacesContext facesContext;
    private String dateString;
    private String dateStringHuman;
    
    private static final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
    private static final Font tableHeadFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
    private static final Font smallNormal = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

    // TODO: exception handling
    public String createStaffMemberRecruitmentOverview() throws DocumentException, FileNotFoundException, IOException {

        Document document = new Document();
        document.setPageSize(PageSize.A4.rotate());     // landscape format

        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"personal-rekrutierungs-uebersicht_" + dateString + ".pdf\"");
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        addMetaData(document);
        addContent(document);
        document.close();

        facesContext.responseComplete();
        return "locationShiftReportCreated";
    }

    private void addMetaData(Document document) {
        document.addTitle("Personal-Rekrutierungs-Übersicht " + dateString);
        document.addSubject("Personal-Rekrutierungs-Übersicht " + dateString + " für den Event");
        document.addKeywords("Event, PDF, Plan, Personal, Helfer, Staff");
        document.addAuthor("Stefan Meichtry");
        document.addCreator("Stefan Meichtry");
    }

    private void addContent(Document document) throws DocumentException {
        // load location data
        List<Location> allLocations = locationJpaController.findLocationEntities();
        Collections.sort(allLocations);
        
        // load all shift data
        List<Shift> allShifts = shiftJpaController.findShiftEntities();
        
        // load all staffMember data
        List<StaffMember> allStaffMembers = staffMemberJpaController.findStaffMemberEntities();
        
        // summary page first
        Paragraph summaryPreface = new Paragraph();
        addEmptyLine(summaryPreface, 1);
        summaryPreface.add(new Paragraph("eventplaner: Personal-Rekrutierungs-Übersicht - Zusammenfassung", catFont));
        addEmptyLine(summaryPreface, 1);
        summaryPreface.add(new Paragraph("generiert: " + dateStringHuman, smallNormal));
        addEmptyLine(summaryPreface, 1);
        document.add(summaryPreface);
        
        int totalEventShiftStaffmemberRequired = 0;
        int totalEventShiftStaffmemberCurrent = 0;
        
        for (Shift shift: allShifts) {
            totalEventShiftStaffmemberRequired += shift.getNumberOfStaffMembers();
            totalEventShiftStaffmemberCurrent += shift.getStaffMembersSize();
        }

        PdfPTable summaryTable = new PdfPTable(new float[]{50, 40});
        summaryTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        summaryTable.addCell(new Paragraph("Event Total Personen:", subFont));
        summaryTable.addCell(new Paragraph(Integer.toString(allStaffMembers.size()), subFont));
        summaryTable.addCell(new Paragraph("Event Total Standorte:", subFont));
        summaryTable.addCell(new Paragraph(Integer.toString(allLocations.size()), subFont));
        summaryTable.addCell(new Paragraph("Event Total Schichten:", subFont));
        summaryTable.addCell(new Paragraph(Integer.toString(allShifts.size()), subFont));        
        summaryTable.addCell(new Paragraph("Event Total Schicht Personen Soll-Bestand:", subFont));
        summaryTable.addCell(new Paragraph(Integer.toString(totalEventShiftStaffmemberRequired), subFont));
        summaryTable.addCell(new Paragraph("Event Total Schicht Personen Ist-Bestand:", subFont));
        summaryTable.addCell(new Paragraph(Integer.toString(totalEventShiftStaffmemberCurrent), subFont));

        document.add(summaryTable);

        document.newPage();

        // show detail of each location
        PdfPTable locationTable = null;
        for (Location location : allLocations) {

            // preface
            Paragraph preface = new Paragraph();
            addEmptyLine(preface, 1);
            preface.add(new Paragraph("eventplaner: Personal-Rekrutierungs-Übersicht - Standort Detail", catFont));
            addEmptyLine(preface, 1);
            preface.add(new Paragraph("generiert: " + dateStringHuman, smallNormal));
            addEmptyLine(preface, 1);
            document.add(preface);

            // location Data
            Paragraph locationDataSection = new Paragraph();
            addEmptyLine(locationDataSection, 1);
            locationDataSection.add(new Paragraph("Standort-Daten:", subFont));
            addEmptyLine(locationDataSection, 1);
            document.add(locationDataSection);

            locationTable = new PdfPTable(new float[]{15, 40});
            locationTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            locationTable.addCell(new Paragraph("Standort Name:", subFont));
            locationTable.addCell(new Paragraph(location.getLocationName(), subFont));
            locationTable.addCell(new Paragraph("Beschreibung:", smallNormal));
            locationTable.addCell(new Paragraph(location.getDescription(), smallNormal));

            document.add(locationTable);

            // shift Data
            Paragraph shiftDataSection = new Paragraph();
            addEmptyLine(shiftDataSection, 1);
            shiftDataSection.add(new Paragraph("Standort Schichten:", subFont));
            addEmptyLine(shiftDataSection, 1);
            document.add(shiftDataSection);

            PdfPTable shiftTable = new PdfPTable(new float[]{18, 18, 25, 18, 18});

            PdfPCell c1 = new PdfPCell(new Phrase("Start", tableHeadFont));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase("Ende", tableHeadFont));
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase("Schicht-Verantwortung", tableHeadFont));
            c3.setHorizontalAlignment(Element.ALIGN_CENTER);
            c3.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c3);

            PdfPCell c4 = new PdfPCell(new Phrase("Soll-Bestand", tableHeadFont));
            c4.setHorizontalAlignment(Element.ALIGN_CENTER);
            c4.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c4);

            PdfPCell c5 = new PdfPCell(new Phrase("Ist-Bestand", tableHeadFont));
            c5.setHorizontalAlignment(Element.ALIGN_CENTER);
            c5.setBackgroundColor(BaseColor.LIGHT_GRAY);
            shiftTable.addCell(c5);

            List<Shift> shifts = (List<Shift>) location.getShifts();
            Collections.sort(shifts);

            int totalLocationStaffmemberRequired = 0;
            int totalLocationStaffmemberCurrent = 0;

            for (Shift shift : shifts) {
                SimpleDateFormat formatter = new SimpleDateFormat("E. dd.MM.yyyy HH:mm", Locale.GERMAN);

                Date startTime = shift.getStartTime();
                Date endTime = shift.getEndTime();

                String startTimeString = formatter.format(startTime);
                String endTimeString = formatter.format(endTime);

                shiftTable.addCell(new Paragraph(startTimeString, smallNormal));
                shiftTable.addCell(new Paragraph(endTimeString, smallNormal));
                if (shift.getResponsible() == null) {
                    shiftTable.addCell(new Paragraph("", smallNormal));
                } else {
                    shiftTable.addCell(new Paragraph(shift.getResponsible().toString(), smallNormal));
                }
                shiftTable.addCell(new Paragraph(Integer.toString(shift.getNumberOfStaffMembers()), smallNormal));
                shiftTable.addCell(new Paragraph(Integer.toString(shift.getStaffMembersSize()), smallNormal));

                totalLocationStaffmemberRequired += shift.getNumberOfStaffMembers();
                totalLocationStaffmemberCurrent += shift.getStaffMembersSize();
            }

            document.add(shiftTable);

            // location statistics
            Paragraph locationStatisticsSection = new Paragraph();
            addEmptyLine(locationStatisticsSection, 1);
            locationStatisticsSection.add(new Paragraph("Standort Statistik:", subFont));
            addEmptyLine(locationStatisticsSection, 1);
            document.add(locationStatisticsSection);

            PdfPTable locationStatisticsTable = new PdfPTable(new float[]{15, 40});
            locationStatisticsTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            locationStatisticsTable.addCell(new Paragraph("Standort Anzahl Schichten:", smallNormal));
            locationStatisticsTable.addCell(new Paragraph(Integer.toString(shifts.size()), smallNormal));
            locationStatisticsTable.addCell(new Paragraph("Standort Total Soll-Bestand:", smallNormal));
            locationStatisticsTable.addCell(new Paragraph(Integer.toString(totalLocationStaffmemberRequired), smallNormal));
            locationStatisticsTable.addCell(new Paragraph("Standort Total Ist-Bestand:", smallNormal));
            locationStatisticsTable.addCell(new Paragraph(Integer.toString(totalLocationStaffmemberCurrent), smallNormal));

            document.add(locationStatisticsTable);

            document.newPage();

        }
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
