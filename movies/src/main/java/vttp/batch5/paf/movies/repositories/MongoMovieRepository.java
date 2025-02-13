package vttp.batch5.paf.movies.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;

import jakarta.json.JsonObject;
import vttp.batch5.paf.movies.models.DirectorStats;

@Repository
public class MongoMovieRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME = "imdb";
    private static final String ERROR_COLLECTION = "errors";

    // TODO: Task 2.3
    // You can add any number of parameters and return any type from the method
    // You can throw any checked exceptions from the method
    // Write the native Mongo query you implement in the method in the comments
    //
    //    native MongoDB query here
    //

    /*
    db.createCollection("imdb")
    db.imdb.createIndex({ "imdb_id": 1 }, { unique: true })
    db.imdb.insertMany([{
        imdb_id: String,
        title: String,
        director: String,
        overview: String,
        tagline: String,
        genres: String,
        imdb_rating: Number,
        imdb_votes: Number
    }])
     */
    public void batchInsertMovies(List<JsonObject> movies) {
        ensureCollectionAndIndex();
        List<WriteModel<Document>> writes = movies.stream()
                .map(this::createInsertModel)
                .toList();
        if (!writes.isEmpty()) {
            try {
                mongoTemplate.getCollection(COLLECTION_NAME)
                        .bulkWrite(writes);
            } catch (Exception e) {
                logError(movies.stream()
                        .map(m -> getStringValue(m, "imdb_id"))
                        .toList(), e);
                throw e;
            }
        }
    }

    private WriteModel<Document> createInsertModel(JsonObject movie) {
        Document doc = convertToMongoDocument(movie);
        return new InsertOneModel<Document>(doc);
    }

    private void ensureCollectionAndIndex() {
        if (!mongoTemplate.collectionExists(COLLECTION_NAME)) {
            mongoTemplate.createCollection(COLLECTION_NAME);
        }
        if (!mongoTemplate.collectionExists(ERROR_COLLECTION)) {
            mongoTemplate.createCollection(ERROR_COLLECTION);
        }
        IndexOperations indexOps = mongoTemplate.indexOps(COLLECTION_NAME);
        IndexDefinition indexDefinition = new Index()
                .on("imdb_id", Sort.Direction.ASC)
                .unique();
        indexOps.ensureIndex(indexDefinition);
    }

    // TODO: Task 2.4
    // You can add any number of parameters and return any type from the method
    // You can throw any checked exceptions from the method
    // Write the native Mongo query you implement in the method in the comments
    //
    //    native MongoDB query here
    //
    /* db.errors.insert({
      ids: ["a0", "a1", "a2", "a3", "a4"],
      error: "Error message here",
      timestamp: new Date()
  })
     */
    public void logError(List<String> imdbIds, Exception ex) {
        Document errorDoc = new Document()
                .append("ids", imdbIds)
                .append("error", ex.getMessage())
                .append("timestamp", new Date());
        mongoTemplate.getCollection(ERROR_COLLECTION).insertOne(errorDoc);
    }

    // TODO: Task 3
    // Write the native Mongo query you implement in the method in the comments
    //
    //    native MongoDB query here
    //
    /*
    db.imdb.aggregate([
  {
    $match: {
      directors: { $ne: "" }
    }
  },{
    $group: {
      _id: "$directors",
      movies_count: { $sum: 1 }
    }
  },{
    $project: {
      _id: 0,
      director_name: "$_id",
      movies_count: 1
    }
  },{
    $sort: { movies_count: -1 }
  },{
    $limit: 10  
  }
])*/
    public List<DirectorStats> getTopDirectors(Integer limit) {
        Aggregation pipeline = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("directors").ne("")),
                Aggregation.group("directors").count().as("movies_count"),
                Aggregation.project()
                        .andExpression("_id").as("director_name")
                        .andExpression("movies_count").as("movies_count"),
                Aggregation.sort(Sort.Direction.DESC, "movies_count"),
                Aggregation.limit(limit));
        AggregationResults<Document> results = mongoTemplate.aggregate(pipeline, COLLECTION_NAME, Document.class);
        List<DirectorStats> directors = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            DirectorStats stats = new DirectorStats(
                    doc.getString("director_name"),
                    doc.getInteger("movies_count"),
                    0.0,
                    0.0,
                    0.0);
            directors.add(stats);
        }
        return directors;
    }

    // db.imdb.find(
    // { "directors": "Director Name" },
    // { "imdb_id": 1, "_id": 0 })
    public List<String> getMovieIdsByDirector(String directorName) {
        Query query = Query.query(
                Criteria.where("directors").is(directorName)
        );
        query.fields().include("imdb_id");
        return mongoTemplate.find(query, Document.class, COLLECTION_NAME)
                .stream()
                .map(doc -> doc.getString("imdb_id"))
                .toList();
    }

    // helper method for task 2
    private Document convertToMongoDocument(JsonObject movie) {
        Document doc = new Document();
        doc.put("imdb_id", getStringValue(movie, "imdb_id"));
        doc.put("title", getStringValue(movie, "title"));
        doc.put("director", getStringValue(movie, "director"));
        doc.put("overview", getStringValue(movie, "overview"));
        doc.put("tagline", getStringValue(movie, "tagline"));
        doc.put("genres", getStringValue(movie, "genres"));
        doc.put("imdb_rating", getDoubleValue(movie, "imdb_rating"));
        doc.put("imdb_votes", getIntValue(movie, "imdb_votes"));
        return doc;
    }

    private String getStringValue(JsonObject json, String key) {
        return json.containsKey(key) && !json.isNull(key)
                ? json.getString(key)
                : "";
    }

    private double getDoubleValue(JsonObject json, String key) {
        return json.containsKey(key) && !json.isNull(key)
                ? json.getJsonNumber(key).doubleValue()
                : 0.0;
    }

    private int getIntValue(JsonObject json, String key) {
        return json.containsKey(key) && !json.isNull(key)
                ? json.getJsonNumber(key).intValue()
                : 0;
    }
}
