package org.itmo;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

@JCStressTest
@Outcome(id = "10", expect = Expect.ACCEPTABLE, desc = "Все вершины были обойдены")
@Outcome(expect = Expect.ACCEPTABLE_INTERESTING, desc = "Гонка данных: часть вершин не была обойдена")
@State
public class BFSJcStressTest {
    private static final int CORES_NUM = 4;
    private static final int VERTICES_NUM = 10;

    private Graph graph = new Graph(VERTICES_NUM);

    AtomicBoolean[] visited = new AtomicBoolean[VERTICES_NUM];
    AtomicInteger nextQueueIdx = new AtomicInteger(0);

    Queue<Integer>[] nextQueueLayer = new Queue[CORES_NUM];

    public BFSJcStressTest() {
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(0, 3);
        graph.addEdge(0, 4);

        graph.addEdge(1, 5);
        graph.addEdge(3, 6);
        graph.addEdge(2, 7);
        graph.addEdge(4, 8);
        graph.addEdge(4, 9);

        for (int i = 0; i < VERTICES_NUM; ++i) {
            visited[i] = new AtomicBoolean(false);
        }

        for (int i = 0; i < CORES_NUM; ++i) {
            nextQueueLayer[i] = new ConcurrentLinkedQueue<>();
        }

        for (int i = 0; i < 5; ++i) {
            visited[i].set(true);
        }
    }

    @Actor
    void actor1() {
        step(1, 3);
    }

    @Actor
    void actor2() {
        step(2, 4);
    }

    void step(int... values) {
        Arrays.stream(values).forEach(startVertex -> {
            for (int vertex : graph.childrenOf(startVertex)) {
                if (!visited[vertex].compareAndSet(false, true)) {
                    continue;
                }

                int selectedQueue = nextQueueIdx.incrementAndGet() % CORES_NUM;
                nextQueueLayer[selectedQueue].add(vertex);
            }
        });
    }

    @Arbiter
    public void arbiter(L_Result r) {
        r.r1 = Stream.of(visited).filter(AtomicBoolean::get).count();
    }
}
