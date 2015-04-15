package ru.ifmo.ctddev.volhov.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author volhovm
 *         Created on 4/15/15
 */
@SuppressWarnings("UnusedDeclaration")
public class WebCrawler implements Crawler {
    ConcurrentHashMap<String, Semaphore> semaphoreMap;
    Downloader downloader;
    ExecutorService downloadService, extractService;
    final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.semaphoreMap = new ConcurrentHashMap<>();
        this.downloader = downloader;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.extractService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    @Override
    public List<String> download(String url, int depth) throws IOException {
        ArrayList<String> ret = new ArrayList<>();
        ArrayDeque<Future<Pair<String, Future<List<String>>>>> downloadFutures = new ArrayDeque<>();
        ArrayDeque<Future<List<String>>> extractFutures = new ArrayDeque<>();
        HashSet<String> visited = new HashSet<>();
        ArrayDeque<String> links = new ArrayDeque<>();
        links.push(url);
        for (int i = depth; i > 0; i--) {
            // add tasks for downloading of this level
            while (!links.isEmpty()) {
                String link = links.removeFirst();
                String host = URLUtils.getHost(link);
                if (!visited.contains(link)) {
                    downloadFutures.addLast(downloadService.submit(() -> {
//                    System.out.println("In downloadService child");
                        if (!semaphoreMap.containsKey(host)) {
                            semaphoreMap.put(host, new Semaphore(perHost));
                        }
                        semaphoreMap.get(host).acquire();
                        Document doc = downloader.download(link);
                        semaphoreMap.get(host).release();
                        return new Pair<>(link, extractService.<List<String>>submit(doc::extractLinks));
                    }));
                }
            }
            // process download tasks
            while (!downloadFutures.isEmpty()) {
                try {
                    Pair<String, Future<List<String>>> pair = downloadFutures.removeFirst().get();
                    visited.add(pair.getKey());
                    ret.add(pair.getKey());
                    extractFutures.add(pair.getValue());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            // process extracting links
            while (!extractFutures.isEmpty()) {
                try {
                    extractFutures.removeFirst().get().stream().forEach(links::addLast);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    @Override
    public void close() {
        downloadService.shutdown();
        extractService.shutdown();
    }
}
