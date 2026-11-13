package com.oslab.memory;

import com.oslab.algorithms.memory.BestFit;
import com.oslab.algorithms.memory.FIFO;
import com.oslab.algorithms.memory.FirstFit;
import com.oslab.algorithms.memory.LRU;
import com.oslab.algorithms.memory.MemoryAllocationAlgorithm;
import com.oslab.algorithms.memory.Optimal;
import com.oslab.algorithms.memory.PageReplacementAlgorithm;
import com.oslab.algorithms.memory.WorstFit;
import com.oslab.service.MemoryAllocationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryAlgorithmsTest {
    @Test
    void firstBestAndWorstFitChooseExpectedPartitions() {
        List<MemoryBlock> blocks = List.of(new MemoryBlock(0, 100),
                new MemoryBlock(100, 300), new MemoryBlock(400, 200));
        MemoryProcess process = new MemoryProcess("P1", 180);

        assertEquals("P1", new FirstFit().allocate(blocks, process).blocks().get(1).processId());
        assertEquals("P1", new BestFit().allocate(blocks, process).blocks().get(2).processId());
        assertEquals("P1", new WorstFit().allocate(blocks, process).blocks().get(1).processId());
    }

    @Test
    void allocationSplitsExactAndLargerBlocks() {
        MemoryAllocationService service = new MemoryAllocationService();
        service.configure(500, List.of(200, 300));
        MemoryAllocationResult result = service.allocate(new FirstFit(), new MemoryProcess("P1", 200));
        assertTrue(result.allocated());
        assertEquals(0, result.internalFragmentation());
        assertEquals(300, result.freeMemory());
        assertEquals(200, result.blocks().get(0).size());
    }

    @Test
    void insufficientMemoryAndExternalFragmentationAreReported() {
        MemoryAllocationService service = new MemoryAllocationService();
        service.configure(300, List.of(100, 100, 100));
        service.allocate(new FirstFit(), new MemoryProcess("P1", 80));
        service.allocate(new FirstFit(), new MemoryProcess("P2", 100));
        MemoryAllocationResult failed = service.allocate(new FirstFit(), new MemoryProcess("P3", 110));
        assertFalse(failed.allocated());
        assertEquals(20, failed.externalFragmentation());
    }

    @Test
    void deallocationCoalescesAdjacentFreePartitions() {
        MemoryAllocationService service = new MemoryAllocationService();
        service.configure(300, List.of(100, 100, 100));
        service.allocate(new FirstFit(), new MemoryProcess("P1", 100));
        service.allocate(new FirstFit(), new MemoryProcess("P2", 100));
        service.deallocate("P1");
        service.deallocate("P2");
        assertEquals(1, service.blocks().size());
        assertEquals(300, service.blocks().get(0).size());
    }

    @Test
    void pageAlgorithmsHandleEmptyReferencesAndRepeatedPages() {
        List<PageReplacementAlgorithm> algorithms = List.of(new FIFO(), new LRU(), new Optimal());
        for (PageReplacementAlgorithm algorithm : algorithms) {
            PageReplacementResult empty = algorithm.simulate(List.of(), 2);
            assertEquals(0, empty.pageFaults());
            PageReplacementResult repeated = algorithm.simulate(List.of(
                    new PageReference(1), new PageReference(1), new PageReference(1)), 2);
            assertEquals(1, repeated.pageFaults());
            assertEquals(2, repeated.pageHits());
        }
    }

    @Test
    void oneFrameProducesFaultForEveryPageChange() {
        PageReplacementResult result = new FIFO().simulate(List.of(
                new PageReference(1), new PageReference(2), new PageReference(1)), 1);
        assertEquals(3, result.pageFaults());
        assertEquals(0, result.pageHits());
        assertEquals(100.0, result.pageFaultRatio());
    }

    @Test
    void allPagesFittingInFramesFaultOnlyOnFirstReference() {
        PageReplacementResult result = new LRU().simulate(List.of(
                new PageReference(1), new PageReference(2), new PageReference(1),
                new PageReference(2)), 2);
        assertEquals(2, result.pageFaults());
        assertEquals(2, result.pageHits());
    }

    @Test
    void pageReplacementReportsReplacedPages() {
        PageReplacementResult result = new Optimal().simulate(List.of(
                new PageReference(1), new PageReference(2), new PageReference(3)), 2);
        assertEquals(3, result.pageFaults());
        assertEquals(1, result.steps().get(2).replacedPage());
    }
}
