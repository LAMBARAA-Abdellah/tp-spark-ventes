package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TotalSalesByCityAndYear {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("VentesParVilleEtAnnee")
                .setMaster("local[*]");

        JavaSparkContext sc = new JavaSparkContext(conf);
        sc.setLogLevel("WARN");

        JavaRDD<String> lignes = sc.textFile("sales.txt");  // Ce fichier doit exister dans le répertoire

        String header = lignes.first(); // "date,city,product,price"
        JavaRDD<String> data = lignes.filter(line -> !line.equals(header));

        JavaPairRDD<String, Double> ventesParVilleAnnee = data.mapToPair(line -> {
            String[] parts = line.split(",");
            String date = parts[0];
            String city = parts[1];
            double price = Double.parseDouble(parts[3]);

            LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int year = parsedDate.getYear();
            String cle = city + "-" + year;

            return new Tuple2<>(cle, price);
        });

        JavaPairRDD<String, Double> totalParVilleAnnee = ventesParVilleAnnee
                .reduceByKey(Double::sum)
                .sortByKey();

        totalParVilleAnnee.collect().forEach(result -> {
            System.out.println(result._1 + " => " + result._2 + " MAD");
        });

        sc.close();
    }
}
