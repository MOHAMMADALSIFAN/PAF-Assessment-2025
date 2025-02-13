package vttp.batch5.paf.movies.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vttp.batch5.paf.movies.models.DirectorStats;
import vttp.batch5.paf.movies.services.MovieService;

@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    private MovieService movieService;

    @Value("${pafassessment.user.name}")
    private String userName;

    @Value("${pafassessment.user.batch}")
    private String userBatch;

    // TODO: Task 3
    @GetMapping("/summary")
    public ResponseEntity<List<DirectorStats>> getAllDirectorsforthelimits(@RequestParam Integer count) {
        List<DirectorStats> result = movieService.getProlificDirectors(count);
        return ResponseEntity.ok(result);
    }
    // TODO: Task 4

    @GetMapping("/summary/pdf")
    public ResponseEntity<byte[]> generateReport(@RequestParam Integer count) {
        byte[] pdfReport = movieService.generatePDFReport(userName, userBatch, count);
        if (pdfReport == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Director_Movies_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfReport);
    }
}
