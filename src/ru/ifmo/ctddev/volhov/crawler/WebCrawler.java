package ru.ifmo.ctddev.volhov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;
import javafx.util.Pair;
import net.java.quickcheck.generator.support.TupleGenerator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
//@SuppressWarnings("UnusedDeclaration")
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloadService, extractService;
    private final int perHost, downloadLimit, extractLimit;
    private final static String USE = "Use: WebCrawler url [downloads [extractors [perHost]]]";

    /**
     * This is the main method, that accepts the parameters in form:
     * {@code url [downloads [extractors [perHost]]]}
     * and starts new entry of this with {@link info.kgeorgiy.java.advanced.crawler.CachingDownloader}
     * on specified url with depth = 1.
     * The files are saved in ./default/ directory.
     *
     * @param args command line arguments
     *
     * @throws IOException thrown if directory can't be created
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
//        this.downloadLimit = Math.min(downloaders, (Integer.MAX_VALUE / 3));
//        this.extractLimit = Math.min(extractors, (Integer.MAX_VALUE / 3));
//        this.perHost = Math.min(perHost, Integer.MAX_VALUE / 2);
        this.downloadLimit = downloaders;
        this.extractLimit = extractors;
        this.perHost = perHost;
        this.downloader = downloader;
        this.downloadService = Executors.newFixedThreadPool(this.downloadLimit);
        this.extractService = Executors.newFixedThreadPool(this.extractLimit);
    }

    private void produceExtract(int depth, Document doc,
                                final ConcurrentHashMap<String, Object> ret,
                                final ConcurrentHashMap<String, Object> visited,
                                final ConcurrentHashMap<String, Semaphore> hostSemaphores,
                                final ConcurrentHashMap<String, ConcurrentLinkedDeque<Pair<String, Integer>>> downloadQueues,
                                final Semaphore clocker) {
        try {
            if (depth > 1) {
                List<String> docLinks = doc.extractLinks();
                docLinks.stream().distinct().forEach(url -> {
                    try {
                        String host = URLUtils.getHost(url);
                        downloadQueues.putIfAbsent(host, new ConcurrentLinkedDeque<>());
                        downloadQueues.get(host).addLast(new Pair<>(url, depth - 1));
                        hostSemaphores.putIfAbsent(host, new Semaphore(perHost));
                        clocker.acquire();
                        downloadService.submit(
                                () -> this.produceDownload(host, ret,
                                        visited, hostSemaphores, downloadQueues, clocker));
                    } catch (MalformedURLException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clocker.release();
        }
    }

    private void produceDownload(
            String host,
            final ConcurrentHashMap<String, Object> ret,
            final ConcurrentHashMap<String, Object> visited,
            final ConcurrentHashMap<String, Semaphore> hostSemaphores,
            final ConcurrentHashMap<String, ConcurrentLinkedDeque<Pair<String, Integer>>> downloadQueues,
            final Semaphore clocker
    ) {
        try {
            if (hostSemaphores.get(host).tryAcquire()) {
                if (!downloadQueues.get(host).isEmpty()) {
                    Pair<String, Integer> pair = downloadQueues.get(host).removeFirst();
                    String url = pair.getKey();
                    int depth = pair.getValue();
                    if (visited.putIfAbsent(url, new Object()) == null) {
                        try {
                            Document doc = downloader.download(url);
                            ret.put(url, new Object());
                            clocker.acquire();
                            extractService.submit(() -> produceExtract(
                                    depth,
                                    doc,
                                    ret,
                                    visited,
                                    hostSemaphores,
                                    downloadQueues,
                                    clocker
                            ));
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            hostSemaphores.get(host).release();
                            if (!downloadQueues.get(host).isEmpty()) {
                                try {
                                    clocker.acquire();
                                    downloadService.submit(
                                            () -> produceDownload(host, ret, visited, hostSemaphores, downloadQueues,
                                                    clocker));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    hostSemaphores.get(host).release();
                }
            }
        } finally {
            clocker.release();
        }
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
        final int semaphorSize = Integer.MAX_VALUE - 20;
        final Semaphore clocker = new Semaphore(semaphorSize); // we clock in on submitting and clock out when ended
        final ConcurrentHashMap<String, Object> ret = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, Object> visited = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, Semaphore> hostSemaphores = new ConcurrentHashMap<>();
        // host -> (url, depth)
        final ConcurrentHashMap<String, ConcurrentLinkedDeque<Pair<String, Integer>>> downloadQueue =
                new ConcurrentHashMap<>();

        // add initial value to queue
        try {
            String host = URLUtils.getHost(url);
            System.out.println("Link: " + url + ", host: " + host);
            hostSemaphores.putIfAbsent(host, new Semaphore(perHost));
            downloadQueue.putIfAbsent(host, new ConcurrentLinkedDeque<>());
            downloadQueue.get(host).addLast(new Pair<>(url, depth));
            clocker.acquire();
            downloadService.submit(
                    () -> this.produceDownload(host, ret, visited, hostSemaphores, downloadQueue, clocker));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        try {
            clocker.acquire(semaphorSize);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("----------------------------------------------OUTOUTOUTOUT");
        return ret.keySet().stream().collect(Collectors.toList());
    }

    /**
     * This method closes this entry of {@link ru.ifmo.ctddev.volhov.crawler.WebCrawler}.
     * Everything that {@link #download} method does doesn't use any cache, so it basically doesn't
     * need this method to be invoked. On the other hand, this needs to be performed in the end
     * because this is the only way to stop threads used inside of this.
     */
    @Override
    public void close() {
        System.out.println("closing the services");
        downloadService.shutdown();
        extractService.shutdown();
        if (!downloadService.isShutdown()) {
            try {
                downloadService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                downloadService.shutdownNow();
            }

        }
        if (!extractService.isShutdown()) {
            try {
                extractService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                extractService.shutdownNow();
            }
        }
    }
}
