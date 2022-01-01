package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.Method;

import java.util.*;
import java.util.stream.Collectors;

public class PathMatcher {

    private Map<String, PathMatcher> children = new LinkedHashMap<>();
    private Map<Method, List<Flow>> flows = new EnumMap<>(Method.class);
    private Map<Method, List<Flow>> wildCardFlows = new EnumMap<>(Method.class);

    public void putRoute(String path, Method method, Flow flow)
    {
        Queue<String> pathSegments = splitPath(path);
        this.putRoute(pathSegments, method, flow);
    }

    private Queue<String> splitPath(String path) {
        return Arrays.stream(path.split("/")).filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(LinkedList::new));
    }

    private void putRoute(Queue<String> pathSegments, Method method, Flow flow) {
        String pathSegment = pathSegments.poll();


        boolean isAWildCard = pathSegment.equals("*");
        if(isAWildCard)
        {
            this.addWildcardHandler(flow, method);
            return;
        }

        PathMatcher childPathMatcher = children.computeIfAbsent(pathSegment, s -> new PathMatcher());
        if(pathSegments.size() == 0)
        {
            childPathMatcher.addHandler(flow, method);
        }
        else
        {
           childPathMatcher.putRoute(pathSegments, method, flow);
        }



    }

    private void addWildcardHandler(Flow flow, Method method) {
        this.wildCardFlows.computeIfAbsent(method, key -> new ArrayList<>()).add(flow);

    }

    private void addHandler(Flow flow, Method method) {
        this.flows.computeIfAbsent(method, key -> new ArrayList<>()).add(flow);
    }

    public List<Flow> get(String path, Method method) {
        Queue<String> pathSegments = splitPath(path);

        return get(pathSegments, method, new ArrayList<>());
    }

    private List<Flow> get(Queue<String> pathSegments, Method method, List<Flow> collectedFlows) {
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
            this.children.get(pathSegment).get(pathSegments, method, collectedFlows);
        }

        return collectedFlows;
    }
}
