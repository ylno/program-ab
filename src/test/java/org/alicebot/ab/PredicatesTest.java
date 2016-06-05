package org.alicebot.ab;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PredicatesTest {


    @Test
    public void testStorageHandlerFromKey() throws Exception {

        Predicates predicates = new Predicates("botname", "1");
        assertThat(predicates.buildStorageHandlerFromKey("storageKey").getKey(), is("botname.1.storageKey"));
        assertThat(predicates.buildStorageHandlerFromKey("storageKey"), instanceOf(StringStorageHandler.class));
    }

    @Test
    public void testBuildStorageHandlerFromKeyGeneral() throws Exception {

        Predicates predicates = new Predicates("botname", "1");

        assertThat(predicates.buildStorageHandlerFromKey("general.storageKey").getKey(), is("botname.general.storageKey"));
        assertThat(predicates.buildStorageHandlerFromKey("general.storageKey"), instanceOf(StringStorageHandler.class));
    }

    @Test
    public void testBuildStorageHandlerList() throws Exception {

        Predicates predicates = new Predicates("botname", "1");

        assertThat(predicates.buildStorageHandlerFromKey("list.storageKey").getKey(), is("botname.1.storageKey"));
        assertThat(predicates.buildStorageHandlerFromKey("list.storageKey"), instanceOf(ListStorageHandler.class));
    }

    @Test
    public void testBuildStorageHandlerListGeneral() throws Exception {

        Predicates predicates = new Predicates("botname", "1");

        assertThat(predicates.buildStorageHandlerFromKey("general.list.storageKey").getKey(), is("botname.general.storageKey"));
        assertThat(predicates.buildStorageHandlerFromKey("general.list.storageKey"), instanceOf(ListStorageHandler.class));
    }

}
