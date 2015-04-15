package ru.ifmo.ctddev.volhov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
 * @author volhovm
 *         Created on 4/15/15
 * @see info.kgeorgiy.java.advanced.crawler.URLUtils
 * @see info.kgeorgiy.java.advanced.crawler.Crawler
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.Semaphore
 */
@SuppressWarnings("UnusedDeclaration")
public class WebCrawler implements Crawler {
    private final ConcurrentHashMap<String, Semaphore> semaphoreMap;
    private final Downloader downloader;
    private final ExecutorService downloadService, extractService;
    private final int perHost;
    private final static String USE = "Use: WebCrawler url [downloads [extractors [perHost]]]";

    /**
     * This is the main method, that accepts the parameters in form:
     * {@code url [downloads [extractors [perHost]]]}
     * and starts new entry of this with {@link info.kgeorgiy.java.advanced.crawler.CachingDownloader}
     * on specified url with depth = 1.
     * The files are saved in ./default/ directory.
     *
     * @param args          command line arguments
     * @throws IOException  thrown if directory can't be created
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 4
                || Arrays.stream(args).filter(a -> a == null).count() > 0) {
            System.err.println(USE);
            return;
        }
        String url = args[0];
        int downloaders = 20;
        int extractors = 20;
        int perHost = 20;
        try {
            if (args.length > 1) {
                downloaders = Integer.parseInt(args[1]);
            }
            if (args.length > 2) {
                extractors = Integer.parseInt(args[2]);
            }
            if (args.length == 4) {
                perHost = Integer.parseInt(args[3]);
            }
        } catch (NumberFormatException ignored) {
            System.err.println(ignored.getMessage());
            System.err.println(USE);
        }
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(new File("./default/")),
                    downloaders, extractors, perHost)) {
            crawler.download(url, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param downloader  the {@link info.kgeorgiy.java.advanced.crawler.Downloader} class
     *                    that specifies how downloading should be done
     * @param downloaders maximum number of threads that can be used to download the page
     * @param extractors  maximum number of threads that can be used to extract links from pages
     * @param perHost     maximum number of threads to download from the same host
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
     * @param url   the url to be taken as the root of the tree
     * @param depth maximum depth of node in the tree -- number of steps <parse, extract links, proceed
     *              to page specified by links> performed
     *
     * @return list of URLs of sites visited
     * @throws IOException when it's impossible to download the page for some reason
     */
    @Override
    public List<String> download(String url, int depth) throws IOException {
        ArrayList<String> ret = new ArrayList<>();
        ArrayDeque<Future<Pair<String, Future<List<String>>>>> downloadFutures = new ArrayDeque<>();
        ArrayDeque<Future<List<String>>> extractFutures = new ArrayDeque<>();
        HashSet<String> visited = new HashSet<>();
        ArrayDeque<String> links = new ArrayDeque<>();
        links.push(url);
        for (int i = 0; i < depth; i++) {
            System.out.println("--- depth " + (i + 1) + " ---");
            // add tasks for downloading of this level
            while (!links.isEmpty()) {
                String link = links.removeFirst();
                String host = URLUtils.getHost(link);
                System.out.println("Link: " + link + ", host: " + host);
                if (!visited.contains(link)) {
                    visited.add(link);
                    downloadFutures.addLast(downloadService.submit(() -> {
                        if (!semaphoreMap.containsKey(host)) {
                            semaphoreMap.put(host, new Semaphore(perHost));
                        }
//                        System.out.println("Before: " + semaphoreMap.get(host).availablePermits());
                        semaphoreMap.get(host).acquire();
//                        System.out.println("Between: " + semaphoreMap.get(host).availablePermits());
                        Document doc;
                        try {
                            doc = downloader.download(link);
                        } finally {
                            semaphoreMap.get(host).release();
//                            System.out.println("After: " + semaphoreMap.get(host).availablePermits());
                        }
                        return new Pair<>(link, extractService.<List<String>>submit(doc::extractLinks));
                    }));
                }
            }
            // process download tasks
            while (!downloadFutures.isEmpty()) {
                Future<Pair<String, Future<List<String>>>> future = downloadFutures.removeFirst();
                try {
                    Pair<String, Future<List<String>>> pair = future.get(5, TimeUnit.SECONDS);
                    ret.add(pair.getKey());
                    extractFutures.add(pair.getValue());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException ignored) {
                    downloadFutures.addLast(future);
                    int counter = 0;
                    for (Future f : downloadFutures) {
                        if (!f.isDone()) {
                            counter++;
                        }
                    }
                    System.out.println("--- Timed out: waiting for " + counter + " downloads to end ---");
                }
            }
            // process extracting links
            while (!extractFutures.isEmpty()) {
                try {
                    extractFutures.removeFirst().get().stream().distinct().forEach(links::addLast);
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
