package io.kiw.luxis.web.test;

import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.internal.RequestPipeline;

import java.util.*;
import java.util.stream.Collectors;

public class PathMatcher {

    private Map<String, PathMatcher> children = new LinkedHashMap<>();
    private String paramName = null;
    private PathMatcher paramChild = null;
    private Map<Method, List<RequestPipeline>> flows = new EnumMap<>(Method.class);
    private Map<Method, List<RequestPipeline>> wildCardFlows = new EnumMap<>(Method.class);
    private List<RequestPipeline> allMethodWildCardFlows = new ArrayList<>();

    public void putRoute(String path, Method method, RequestPipeline flow)
    {
        Queue<String> pathSegments = splitPath(path);
        this.putRoute(pathSegments, method, flow);
    }

    private Queue<String> splitPath(String path) {
        return Arrays.stream(path.split("/")).filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(LinkedList::new));
    }

    private void putRoute(Queue<String> pathSegments, Method method, RequestPipeline flow) {
        String pathSegment = pathSegments.poll();


        boolean isAWildCard = pathSegment.equals("*");
        if(isAWildCard)
        {
            this.addWildcardHandler(flow, method);
            return;
        }

        boolean isAParam = pathSegment.startsWith(":");
        PathMatcher childPathMatcher;
        if(isAParam)
        {
            this.paramName = pathSegment.substring(1);
            this.paramChild = this.paramChild != null ? this.paramChild : new PathMatcher();
            childPathMatcher = this.paramChild;
        }
        else
        {
            childPathMatcher = children.computeIfAbsent(pathSegment, s -> new PathMatcher());
        }

        if(pathSegments.size() == 0)
        {
            childPathMatcher.addHandler(flow, method);
        }
        else
        {
           childPathMatcher.putRoute(pathSegments, method, flow);
        }



    }

    public void putAllMethodRoute(String path, RequestPipeline flow) {
        Queue<String> pathSegments = splitPath(path);
        this.putAllMethodRoute(pathSegments, flow);
    }

    private void putAllMethodRoute(Queue<String> pathSegments, RequestPipeline flow) {
        String pathSegment = pathSegments.poll();

        boolean isAWildCard = pathSegment.equals("*");
        if(isAWildCard)
        {
            this.allMethodWildCardFlows.add(flow);
            return;
        }

        PathMatcher childPathMatcher = children.computeIfAbsent(pathSegment, s -> new PathMatcher());

        if(pathSegments.size() == 0)
        {
            childPathMatcher.allMethodWildCardFlows.add(flow);
        }
        else
        {
            childPathMatcher.putAllMethodRoute(pathSegments, flow);
        }
    }

    private void addWildcardHandler(RequestPipeline flow, Method method) {
        this.wildCardFlows.computeIfAbsent(method, key -> new ArrayList<>()).add(flow);

    }

    private void addHandler(RequestPipeline flow, Method method) {
        this.flows.computeIfAbsent(method, key -> new ArrayList<>()).add(flow);
    }

    public MatchResult get(String path, Method method) {
        Queue<String> pathSegments = splitPath(path);
        Map<String, String> pathParams = new LinkedHashMap<>();

        List<RequestPipeline> flows = get(pathSegments, method, new ArrayList<>(), pathParams);
        return new MatchResult(flows, pathParams);
    }

    private List<RequestPipeline> get(Queue<String> pathSegments, Method method, List<RequestPipeline> collectedFlows, Map<String, String> pathParams) {
        collectedFlows.addAll(this.allMethodWildCardFlows);
        if(this.wildCardFlows.containsKey(method))
        {
            collectedFlows.addAll(this.wildCardFlows.get(method));
        }
        String pathSegment = pathSegments.poll();

        if(pathSegment == null)
        {
            if(this.flows.containsKey(method))
            {
                collectedFlows.addAll(this.flows.get(method));
            }
        }
        else
        {
            PathMatcher child = this.children.get(pathSegment);
            if(child != null)
            {
                child.get(pathSegments, method, collectedFlows, pathParams);
            }
            else if(this.paramChild != null)
            {
                pathParams.put(this.paramName, pathSegment);
                this.paramChild.get(pathSegments, method, collectedFlows, pathParams);
            }
        }

        return collectedFlows;
    }

    public static class MatchResult {
        private final List<RequestPipeline> flows;
        private final Map<String, String> pathParams;

        public MatchResult(List<RequestPipeline> flows, Map<String, String> pathParams) {
            this.flows = flows;
            this.pathParams = pathParams;
        }

        public List<RequestPipeline> getFlows() {
            return flows;
        }

        public Map<String, String> getPathParams() {
            return pathParams;
        }
    }
}
