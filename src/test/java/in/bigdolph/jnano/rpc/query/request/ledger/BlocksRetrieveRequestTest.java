package in.bigdolph.jnano.rpc.query.request.ledger;

import in.bigdolph.jnano.model.block.SendBlock;
import in.bigdolph.jnano.rpc.query.QueryBaseTest;
import in.bigdolph.jnano.rpc.query.response.BlocksResponse;
import in.bigdolph.jnano.tests.NodeTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

public class BlocksRetrieveRequestTest extends QueryBaseTest {
    
    @Test
    @Category(NodeTests.class)
    public void test() {
        BlocksResponse res = query(new BlocksRetrieveRequest(TEST_BLOCK_SEND, TEST_BLOCK_CHANGE, TEST_BLOCK_RECEIVE));
        assertNotNull(res.getBlocks());
        assertEquals(3, res.getBlocks().size());
        
        assertEquals("3928845117595383247300999000000", ((SendBlock)res.getBlock(TEST_BLOCK_SEND)).getNewBalance().toString()); //Ensure balance is accurate
    }
    
}