package com.mindsnacks.zinc.data;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.PathHelper;
import com.mindsnacks.zinc.classes.data.ZincCatalog;
import com.mindsnacks.zinc.classes.data.ZincCatalogs;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * User: NachoSoto
 * Date: 10/7/13
 */
public class ZincCatalogsTest extends ZincBaseTest {
    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Mock ZincJobFactory mJobFactory;
    @Mock FileHelper mFileHelper;

    private ZincCatalogs catalogs;

    private final String mCatalogID = "com.mindsnacks.games";

    @Before
    public void setUp() throws Exception {
        catalogs = new ZincCatalogs(rootFolder.getRoot(), mFileHelper, mJobFactory);
    }

    @Test
    public void returnsLocalCatalogIfExists() throws Exception {
        final ZincCatalog expectedResult = mock(ZincCatalog.class);

        TestFactory.createFile(rootFolder, PathHelper.getLocalCatalogFilePath(mCatalogID), "");
        doReturn(expectedResult).when(mFileHelper).readJSON(any(File.class), eq(ZincCatalog.class));

        // run
        final Future<ZincCatalog> catalog = catalogs.getCatalog(mCatalogID);

        // verify
        assertNotNull(catalog);
        assertEquals(expectedResult, catalog.get());
    }
}
