package vttp.batch5.paf.movies.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.json.data.JsonDataSource;
import vttp.batch5.paf.movies.models.DirectorStats;
import vttp.batch5.paf.movies.repositories.MongoMovieRepository;
import vttp.batch5.paf.movies.repositories.MySQLMovieRepository;

@Service
public class MovieService {

    @Autowired
    private MongoMovieRepository mongoRepo;

    @Autowired
    private MySQLMovieRepository mysqlRepo;

    // TODO: Task 2
    //All done in DataLoader.class
    // TODO: Task 3
    // You may change the signature of this method by passing any number of parameters
    // and returning any type
    public List<DirectorStats> getProlificDirectors(Integer count) {
        List<DirectorStats> directors = mongoRepo.getTopDirectors(count);
        for (DirectorStats director : directors) {
            List<String> movieIds = mongoRepo.getMovieIdsByDirector(director.getDirector_name());
            DirectorStats financials = mysqlRepo.getDirectorFinancials(movieIds);
            Integer movieCount = director.getMovies_count();
            director.setTotal_revenue(financials.getTotal_revenue());
            director.setTotal_budget(financials.getTotal_budget());
            director.setProfit_loss(financials.getTotal_revenue() - financials.getTotal_budget());
            director.setMovies_count(movieCount);
        }

        return directors;
    }

    // TODO: Task 4
    // You may change the signature of this method by passing any number of parameters
    // and returning any type
    public byte[] generatePDFReport(String userName, String userBatch, Integer count) {
        try {
            List<DirectorStats> directors = getProlificDirectors(count);
            JsonObjectBuilder reportBuilder = Json.createObjectBuilder();
            reportBuilder.add("name", userName);
            reportBuilder.add("batch", userBatch);
            JsonObject reportData = reportBuilder.build();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (DirectorStats director : directors) {
                JsonObjectBuilder directorBuilder = Json.createObjectBuilder();
                directorBuilder.add("director", director.getDirector_name());
                directorBuilder.add("count", director.getMovies_count());
                directorBuilder.add("revenue", director.getTotal_revenue());
                directorBuilder.add("budget", director.getTotal_budget());
                arrayBuilder.add(directorBuilder);
            }
            ClassPathResource reportResource = new ClassPathResource("data/director_movies_report.jasper");
            if (!reportResource.exists()) {
                throw new RuntimeException("Report template not found in classpath: data/director_movies_report.jasper");
            }
            try (InputStream jasperStream = reportResource.getInputStream()) {
                JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
                JsonDataSource reportDs = new JsonDataSource(
                    new ByteArrayInputStream(reportData.toString().getBytes()));
                JsonDataSource directorsDs = new JsonDataSource(
                    new ByteArrayInputStream(arrayBuilder.build().toString().getBytes()));
                Map<String, Object> params = new HashMap<>();
                params.put("DIRECTOR_TABLE_DATASET", directorsDs);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, reportDs);
                return JasperExportManager.exportReportToPdf(jasperPrint);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}