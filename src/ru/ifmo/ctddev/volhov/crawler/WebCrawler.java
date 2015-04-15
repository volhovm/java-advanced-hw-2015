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
 * This class is the concurrent implementation of {@link info.kgeorgiy.java.advanced.crawler.Crawler} interface.
 * It allows to download the desired site recursively with the depth specified in parameter of method {@link #download}.
 * <p>
 * Class can be constructed with some restrictions on simultaneous downloads -- downloads limit, links parsing limit
 * and downloads per host limit. Inside, it's achieved with use of java concurrency utils --
 * {@link java.util.concurrent.ExecutorService} and {@link java.util.concurrent.Semaphore}. Some concurrent basic
 * data structures are also used. The algorithm is iterative and similar to BFS. Links to previously visited
 * nodes are ignored.
 * <p>
 * The class implements the {@link java.lang.AutoCloseable} interface and, basically, once it was stopped, it
 * can't be used again.
 * <p>
 * It is needed to pass your own {@link info.kgeorgiy.java.advanced.crawler.Downloader} to the {@link #download}
 * method. It also uses {@link info.kgeorgiy.java.advanced.crawler.URLUtils#getHost} for controlling host limit.
 *
 * @see info.kgeorgiy.java.advanced.crawler.URLUtils
 * @see info.kgeorgiy.java.advanced.crawler.Crawler
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.Semaphore
 * @author volhovm
 *         Created on 4/15/15
 */
@SuppressWarnings("UnusedDeclaration")
public class WebCrawler implements Crawler {
    private final ConcurrentHashMap<String, Semaphore> semaphoreMap;
    private final Downloader downloader;
    private final ExecutorService downloadService, extractService;
    private final int perHost;

    /**
     * @param downloader    the {@link info.kgeorgiy.java.advanced.crawler.Downloader} class
     *                      that specifies how downloading should be done
     * @param downloaders   maximum number of threads that can be used to download the page
     * @param extractors    maximum number of threads that can be used to extract links from pages
     * @param perHost       maximum number of threads to download from the same host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.semaphoreMap = new ConcurrentHashMap<>();
        this.downloader = downloader;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.extractService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    /**
     * This method returns the list of visited pages with respect to BFS tree traversal, where nodes are
     * pages themselves, and every edge is a hyperlink (what is hyperlink and what is not is specified
     * by method {@link info.kgeorgiy.java.advanced.crawler.Document#extractLinks} of class
     * {@link info.kgeorgiy.java.advanced.crawler.Document} that is returned by
     * {@link info.kgeorgiy.java.advanced.crawler.Downloader} specified in constructor of this.
     *
     * @param url           the url to be taken as the root of the tree
     * @param depth         maximum depth of node in the tree -- number of steps <parse, extract links, proceed
     *                      to page specified by links> performed
     * @return              list of URLs of sites visited
     * @throws IOException  when it's impossible to download the page for some reason
     */
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
                System.out.println("Link: " + link + ", host: " + host);
                if (!visited.contains(link)) {
                    downloadFutures.addLast(downloadService.submit(() -> {
//                    System.out.println("In downloadService child");
                        if (!semaphoreMap.containsKey(host)) {
                            semaphoreMap.put(host, new Semaphore(perHost));
                        }
                        System.out.println("Before: " + semaphoreMap.get(host).availablePermits());
                        semaphoreMap.get(host).acquire();
                        System.out.println("Between: " + semaphoreMap.get(host).availablePermits());
                        Document doc = downloader.download(link);
                        System.out.println("After: " + semaphoreMap.get(host).availablePermits());
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

    /**
     * This method closes this entry of {@link ru.ifmo.ctddev.volhov.crawler.WebCrawler}.
     * Everything that {@link #download} method does doesn't use any cache, so it basically doesn't
     * need {@link #close} to be invoked. On the other hand, this needs to be performed in the end
     * because this is the only way to stop threads used inside of this.
     */
    @Override
    public void close() {
        downloadService.shutdown();
        extractService.shutdown();
    }
}
