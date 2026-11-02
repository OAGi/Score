package org.oagi.score.gateway.http.api.module_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.module_management.controller.payload.CopyModuleSetModuleRequest;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSummaryRecord;
import org.oagi.score.gateway.http.api.module_management.model.ModuleType;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleCommandRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copying a module over a same-named sibling must update it instead of inserting a duplicate.
 */
class ModuleSetModuleCommandServiceCopyTest {

    private static final ModuleSetId SET_ID = new ModuleSetId(BigInteger.valueOf(2));
    private static final ModuleId PARENT_ID = new ModuleId(BigInteger.valueOf(10));
    private static final ModuleId EXISTING_ID = new ModuleId(BigInteger.valueOf(11));
    private static final ModuleId SOURCE_ID = new ModuleId(BigInteger.valueOf(20));

    private final ScoreUser requester =
            new ScoreUser(new UserId(BigInteger.ONE), "developer", "Developer", null, false, List.of(ScoreRole.DEVELOPER));
    private ModuleSetModuleCommandService service;
    private ModuleQueryRepository query;
    private ModuleCommandRepository command;

    private static ModuleSummaryRecord module(ModuleId id, ModuleId parent, ModuleType type, String path, String name) {
        return new ModuleSummaryRecord(id, SET_ID, parent, type, path, null, null, name, "1.0");
    }

    @BeforeEach
    void setUp() {
        service = new ModuleSetModuleCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        query = mock(ModuleQueryRepository.class);
        command = mock(ModuleCommandRepository.class);
        when(repositoryFactory.moduleQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.moduleCommandRepository(any())).thenReturn(command);

        when(query.getModule(PARENT_ID)).thenReturn(module(PARENT_ID, null, ModuleType.DIRECTORY, "", "root"));
    }

    @Test
    void sameNamedFileIsUpdatedNotDuplicated() {
        ModuleSummaryRecord source = module(SOURCE_ID, null, ModuleType.FILE, "dup.xsd", "dup.xsd");
        ModuleSummaryRecord existing = module(EXISTING_ID, PARENT_ID, ModuleType.FILE, "dup.xsd", "dup.xsd");
        when(query.getModule(SOURCE_ID)).thenReturn(source);
        when(query.hasDuplicateName(PARENT_ID, "dup.xsd")).thenReturn(true);
        when(query.getDuplicateModule(PARENT_ID, "dup.xsd")).thenReturn(existing);

        service.copyModule(requester, new CopyModuleSetModuleRequest(SET_ID, PARENT_ID, SOURCE_ID, true));

        verify(command).updateVersionNumAndNamespaceId(EXISTING_ID, null, "1.0");
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void childrenOfAMergedDirectoryAreMatchedByName() {
        ModuleId childSourceId = new ModuleId(BigInteger.valueOf(21));
        ModuleId childExistingId = new ModuleId(BigInteger.valueOf(12));
        ModuleSummaryRecord sourceDir = module(SOURCE_ID, null, ModuleType.DIRECTORY, "dir", "dir");
        ModuleSummaryRecord existingDir = module(EXISTING_ID, PARENT_ID, ModuleType.DIRECTORY, "dir", "dir");
        ModuleSummaryRecord sourceChild = module(childSourceId, SOURCE_ID, ModuleType.FILE, "dir\\a.xsd", "a.xsd");
        ModuleSummaryRecord existingChild = module(childExistingId, EXISTING_ID, ModuleType.FILE, "dir\\a.xsd", "a.xsd");
        when(query.getModule(SOURCE_ID)).thenReturn(sourceDir);
        when(query.hasDuplicateName(PARENT_ID, "dir")).thenReturn(true);
        when(query.getDuplicateModule(PARENT_ID, "dir")).thenReturn(existingDir);
        when(query.getChildren(SOURCE_ID)).thenReturn(List.of(sourceChild));
        when(query.hasDuplicateName(EXISTING_ID, "a.xsd")).thenReturn(true);
        when(query.getDuplicateModule(EXISTING_ID, "a.xsd")).thenReturn(existingChild);

        service.copyModule(requester, new CopyModuleSetModuleRequest(SET_ID, PARENT_ID, SOURCE_ID, true));

        verify(command).updateVersionNumAndNamespaceId(childExistingId, null, "1.0");
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void replacingADirectoryWithAFileRemovesItsChildrenAndInsertsTheFile() {
        ModuleId oldChildId = new ModuleId(BigInteger.valueOf(13));
        ModuleSummaryRecord source = module(SOURCE_ID, null, ModuleType.FILE, "x", "x");
        ModuleSummaryRecord existingDir = module(EXISTING_ID, PARENT_ID, ModuleType.DIRECTORY, "x", "x");
        ModuleSummaryRecord oldChild = module(oldChildId, EXISTING_ID, ModuleType.FILE, "x\\y", "y");
        when(query.getModule(SOURCE_ID)).thenReturn(source);
        when(query.hasDuplicateName(PARENT_ID, "x")).thenReturn(true);
        when(query.getDuplicateModule(PARENT_ID, "x")).thenReturn(existingDir);
        when(query.getChildren(EXISTING_ID)).thenReturn(List.of(oldChild));

        service.copyModule(requester, new CopyModuleSetModuleRequest(SET_ID, PARENT_ID, SOURCE_ID, false));

        verify(command).delete(oldChildId);
        verify(command).delete(EXISTING_ID);
        verify(command).create(eq(SET_ID), eq(PARENT_ID), any(), eq(ModuleType.FILE), eq("x"), eq("x"), eq("1.0"));
    }

    private void copy(boolean copySubModules) {
        service.copyModule(requester, new CopyModuleSetModuleRequest(SET_ID, PARENT_ID, SOURCE_ID, copySubModules));
    }

    private void existing(ModuleSummaryRecord source, ModuleSummaryRecord existing) {
        when(query.getModule(SOURCE_ID)).thenReturn(source);
        when(query.hasDuplicateName(PARENT_ID, source.name())).thenReturn(true);
        when(query.getDuplicateModule(PARENT_ID, source.name())).thenReturn(existing);
    }

    @Test
    void aModuleWithoutADuplicateNameIsInserted() {
        when(query.getModule(SOURCE_ID)).thenReturn(module(SOURCE_ID, null, ModuleType.FILE, "new.xsd", "new.xsd"));

        copy(true);

        verify(command).create(eq(SET_ID), eq(PARENT_ID), any(), eq(ModuleType.FILE), eq("new.xsd"), eq("new.xsd"), eq("1.0"));
        verify(command, never()).updateVersionNumAndNamespaceId(any(), any(), any());
        verify(command, never()).delete(any());
    }

    @Test
    void sameNamedFileIsUpdatedWithoutCopySubModules() {
        existing(module(SOURCE_ID, null, ModuleType.FILE, "dup.xsd", "dup.xsd"),
                module(EXISTING_ID, PARENT_ID, ModuleType.FILE, "dup.xsd", "dup.xsd"));

        copy(false);

        verify(command).updateVersionNumAndNamespaceId(EXISTING_ID, null, "1.0");
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void replacingAFileWithADirectoryChangesItsTypeAndInsertsTheNewChildren() {
        ModuleId childSourceId = new ModuleId(BigInteger.valueOf(21));
        existing(module(SOURCE_ID, null, ModuleType.DIRECTORY, "dir", "dir"),
                module(EXISTING_ID, PARENT_ID, ModuleType.FILE, "dir", "dir"));
        when(query.getChildren(SOURCE_ID)).thenReturn(List.of(
                module(childSourceId, SOURCE_ID, ModuleType.FILE, "dir\\a.xsd", "a.xsd")));

        copy(true);

        verify(command).updateVersionNumAndNamespaceIdAndType(EXISTING_ID, null, "1.0", ModuleType.DIRECTORY);
        verify(command).create(eq(SET_ID), eq(EXISTING_ID), any(), eq(ModuleType.FILE), eq("dir\\a.xsd"), eq("a.xsd"), eq("1.0"));
        verify(command, never()).delete(any());
    }

    @Test
    void replacingAFileWithADirectoryWithoutCopySubModulesCopiesNoChildren() {
        existing(module(SOURCE_ID, null, ModuleType.DIRECTORY, "dir", "dir"),
                module(EXISTING_ID, PARENT_ID, ModuleType.FILE, "dir", "dir"));

        copy(false);

        verify(command).updateVersionNumAndNamespaceIdAndType(EXISTING_ID, null, "1.0", ModuleType.DIRECTORY);
        verify(query, never()).getChildren(SOURCE_ID);
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void mergingDirectoriesWithoutCopySubModulesOnlyUpdatesTheDirectory() {
        existing(module(SOURCE_ID, null, ModuleType.DIRECTORY, "dir", "dir"),
                module(EXISTING_ID, PARENT_ID, ModuleType.DIRECTORY, "dir", "dir"));

        copy(false);

        verify(command).updateVersionNumAndNamespaceId(EXISTING_ID, null, "1.0");
        verify(query, never()).getChildren(SOURCE_ID);
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void mergingDirectoriesInsertsTheChildrenThatHaveNoSameNamedModule() {
        ModuleId childSourceId = new ModuleId(BigInteger.valueOf(21));
        existing(module(SOURCE_ID, null, ModuleType.DIRECTORY, "dir", "dir"),
                module(EXISTING_ID, PARENT_ID, ModuleType.DIRECTORY, "dir", "dir"));
        when(query.getChildren(SOURCE_ID)).thenReturn(List.of(
                module(childSourceId, SOURCE_ID, ModuleType.FILE, "dir\\b.xsd", "b.xsd")));

        copy(true);

        verify(command).updateVersionNumAndNamespaceId(EXISTING_ID, null, "1.0");
        verify(command).create(eq(SET_ID), eq(EXISTING_ID), any(), eq(ModuleType.FILE), eq("dir\\b.xsd"), eq("b.xsd"), eq("1.0"));
    }

    @Test
    void replacingADirectoryWithAFileInsertsTheFileWhenSubModulesAreCopiedToo() {
        ModuleId oldChildId = new ModuleId(BigInteger.valueOf(13));
        existing(module(SOURCE_ID, null, ModuleType.FILE, "x", "x"),
                module(EXISTING_ID, PARENT_ID, ModuleType.DIRECTORY, "x", "x"));
        when(query.getChildren(EXISTING_ID)).thenReturn(List.of(
                module(oldChildId, EXISTING_ID, ModuleType.FILE, "x\\y", "y")));

        copy(true);

        verify(command).delete(oldChildId);
        verify(command).delete(EXISTING_ID);
        verify(command).create(eq(SET_ID), eq(PARENT_ID), any(), eq(ModuleType.FILE), eq("x"), eq("x"), eq("1.0"));
    }

    private static ModuleId id(long value) {
        return new ModuleId(BigInteger.valueOf(value));
    }

    /** The created module is what {@code getModule} finds afterwards, so the children are inserted under it. */
    private void createdAs(ModuleId createdId, ModuleId parentId, ModuleType type, String path, String name) {
        when(command.create(eq(SET_ID), eq(parentId), any(), eq(type), eq(path), eq(name), any())).thenReturn(createdId);
        when(query.getModule(createdId)).thenReturn(module(createdId, parentId, type, path, name));
    }

    @Test
    void aNewDirectoryIsInsertedWithItsChildrenWhenSubModulesAreCopied() {
        ModuleId createdDirId = id(30);
        ModuleSummaryRecord sourceDir = module(SOURCE_ID, null, ModuleType.DIRECTORY, "nd", "nd");
        ModuleSummaryRecord sourceSub = module(id(21), SOURCE_ID, ModuleType.DIRECTORY, "nd\\sub", "sub");
        ModuleSummaryRecord sourceLeaf = module(id(22), id(21), ModuleType.FILE, "nd\\sub\\n.xsd", "n.xsd");
        when(query.getModule(SOURCE_ID)).thenReturn(sourceDir);
        when(query.getChildren(SOURCE_ID)).thenReturn(List.of(sourceSub));
        when(query.getChildren(id(21))).thenReturn(List.of(sourceLeaf));
        createdAs(createdDirId, PARENT_ID, ModuleType.DIRECTORY, "nd", "nd");
        createdAs(id(31), createdDirId, ModuleType.DIRECTORY, "nd\\sub", "sub");

        copy(true);

        verify(command).create(eq(SET_ID), eq(createdDirId), any(), eq(ModuleType.DIRECTORY), eq("nd\\sub"), eq("sub"), eq("1.0"));
        verify(command).create(eq(SET_ID), eq(id(31)), any(), eq(ModuleType.FILE), eq("nd\\sub\\n.xsd"), eq("n.xsd"), eq("1.0"));
        verify(command, never()).updateVersionNumAndNamespaceId(any(), any(), any());
        verify(command, never()).delete(any());
    }

    @Test
    void aNewDirectoryIsInsertedAloneWithoutCopySubModules() {
        when(query.getModule(SOURCE_ID)).thenReturn(module(SOURCE_ID, null, ModuleType.DIRECTORY, "nd", "nd"));
        when(query.getChildren(SOURCE_ID)).thenReturn(List.of(
                module(id(21), SOURCE_ID, ModuleType.FILE, "nd\\n.xsd", "n.xsd")));

        copy(false);

        verify(command).create(eq(SET_ID), eq(PARENT_ID), any(), eq(ModuleType.DIRECTORY), eq("nd"), eq("nd"), eq("1.0"));
        verify(query, never()).getChildren(SOURCE_ID);
        verify(command, times(1)).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void grandchildrenOfAMergedDirectoryAreMatchedByNameToo() {
        ModuleId sourceSubId = id(21);
        ModuleId existingSubId = id(12);
        ModuleId existingLeafId = id(14);
        existing(module(SOURCE_ID, null, ModuleType.DIRECTORY, "dir", "dir"),
                module(EXISTING_ID, PARENT_ID, ModuleType.DIRECTORY, "dir", "dir"));
        when(query.getChildren(SOURCE_ID)).thenReturn(List.of(
                module(sourceSubId, SOURCE_ID, ModuleType.DIRECTORY, "dir\\sub", "sub")));
        when(query.getChildren(sourceSubId)).thenReturn(List.of(
                module(id(22), sourceSubId, ModuleType.FILE, "dir\\sub\\g.xsd", "g.xsd")));
        when(query.hasDuplicateName(EXISTING_ID, "sub")).thenReturn(true);
        when(query.getDuplicateModule(EXISTING_ID, "sub")).thenReturn(
                module(existingSubId, EXISTING_ID, ModuleType.DIRECTORY, "dir\\sub", "sub"));
        when(query.hasDuplicateName(existingSubId, "g.xsd")).thenReturn(true);
        when(query.getDuplicateModule(existingSubId, "g.xsd")).thenReturn(
                module(existingLeafId, existingSubId, ModuleType.FILE, "dir\\sub\\g.xsd", "g.xsd"));

        copy(true);

        verify(command).updateVersionNumAndNamespaceId(existingSubId, null, "1.0");
        verify(command).updateVersionNumAndNamespaceId(existingLeafId, null, "1.0");
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void theNamespaceAndVersionOfTheSourceAreCopiedOntoTheExistingModule() {
        NamespaceId namespaceId = new NamespaceId(BigInteger.valueOf(7));
        existing(new ModuleSummaryRecord(SOURCE_ID, SET_ID, null, ModuleType.FILE, "dup.xsd", namespaceId, null, "dup.xsd", "2.0"),
                module(EXISTING_ID, PARENT_ID, ModuleType.FILE, "dup.xsd", "dup.xsd"));

        copy(true);

        verify(command).updateVersionNumAndNamespaceId(EXISTING_ID, namespaceId, "2.0");
    }

    @Test
    void aReplacedDirectoryIsDeletedFromTheBottomUpBeforeTheFileIsInserted() {
        ModuleId subId = id(13);
        ModuleId leafId = id(14);
        existing(module(SOURCE_ID, null, ModuleType.FILE, "x", "x"),
                module(EXISTING_ID, PARENT_ID, ModuleType.DIRECTORY, "x", "x"));
        when(query.getChildren(EXISTING_ID)).thenReturn(List.of(
                module(subId, EXISTING_ID, ModuleType.DIRECTORY, "x\\y", "y")));
        when(query.getChildren(subId)).thenReturn(List.of(
                module(leafId, subId, ModuleType.FILE, "x\\y\\z", "z")));

        copy(false);

        InOrder order = inOrder(command);
        order.verify(command).delete(leafId);
        order.verify(command).delete(subId);
        order.verify(command).delete(EXISTING_ID);
        order.verify(command).create(eq(SET_ID), eq(PARENT_ID), any(), eq(ModuleType.FILE), eq("x"), eq("x"), eq("1.0"));
    }
}
