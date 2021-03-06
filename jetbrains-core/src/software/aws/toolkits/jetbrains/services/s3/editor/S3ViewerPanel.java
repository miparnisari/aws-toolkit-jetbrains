// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.s3.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.ui.ColumnInfo;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import software.aws.toolkits.core.region.AwsRegion;
import software.aws.toolkits.jetbrains.core.credentials.ProjectAccountSettingsManager;
import software.aws.toolkits.jetbrains.services.s3.S3UtilsKt;
import software.aws.toolkits.jetbrains.services.s3.objectActions.CopyPathAction;
import software.aws.toolkits.jetbrains.services.s3.objectActions.DeleteObjectAction;
import software.aws.toolkits.jetbrains.services.s3.objectActions.DownloadObjectAction;
import software.aws.toolkits.jetbrains.services.s3.objectActions.NewFolderAction;
import software.aws.toolkits.jetbrains.services.s3.objectActions.RenameObjectAction;
import software.aws.toolkits.jetbrains.services.s3.objectActions.UploadObjectAction;
import software.aws.toolkits.jetbrains.services.s3.resources.S3Resources;
import software.aws.toolkits.jetbrains.ui.tree.AsyncTreeModel;
import software.aws.toolkits.jetbrains.ui.tree.StructureTreeModel;

@SuppressWarnings("unchecked")
public class S3ViewerPanel {
    private Disposable disposable;
    private JPanel content;
    private JTextField name;
    private JTextField date;
    private JPanel mainPanel;
    private JTextField arnText;
    private S3TreeTable treeTable;
    private S3TreeNode s3TreeNode;
    private S3TreeTableModel model;
    private Project project;

    public S3ViewerPanel(Disposable disposable, Project project, S3VirtualBucket bucketVirtual) {
        this.disposable = disposable;
        this.project = project;

        name.setText(bucketVirtual.getName());
        date.setText(S3Resources.formatDate(bucketVirtual.getS3Bucket().creationDate()));

        AwsRegion activeRegion = ProjectAccountSettingsManager.getInstance(project).getActiveRegion();
        arnText.setText(S3UtilsKt.bucketArn(bucketVirtual.getName(), activeRegion));

        s3TreeNode = new S3TreeDirectoryNode(bucketVirtual, null, "");

        ColumnInfo<Object, String> key = new S3Column(S3ColumnType.NAME);
        ColumnInfo<Object, String> size = new S3Column(S3ColumnType.SIZE);
        ColumnInfo<Object, String> modified = new S3Column(S3ColumnType.LAST_MODIFIED);
        ColumnInfo<Object, String>[] columns = new ColumnInfo[] {key, size, modified};
        model = createTreeTableModel(columns);

        DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();
        tableRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        treeTable = new S3TreeTable(model, bucketVirtual, project);
        treeTable.setRootVisible(false);
        treeTable.setDefaultRenderer(Object.class, tableRenderer);
        S3TreeCellRenderer treeRenderer = new S3TreeCellRenderer(treeTable);
        treeTable.setTreeCellRenderer(treeRenderer);
        treeTable.setCellSelectionEnabled(false);

        treeTable.setRowSelectionAllowed(true);
        treeTable.setAutoCreateRowSorter(true);
        treeTable.setRowSorter(new S3RowSorter(treeTable.getModel()));

        addTreeActions();

        treeTable.getColumnModel().getColumn(1).setMaxWidth(120);

        mainPanel.add(ScrollPaneFactory.createScrollPane(treeTable), BorderLayout.CENTER);
    }

    private void createUIComponents() {
    }

    public JComponent getComponent() {
        return content;
    }

    public JComponent getFocusComponent() {
        return treeTable;
    }

    public JTextField getName() {
        return name;
    }

    private S3TreeTableModel createTreeTableModel(ColumnInfo[] columns) {
        SimpleTreeStructure treeStructure = new SimpleTreeStructure.Impl(s3TreeNode);
        StructureTreeModel<SimpleTreeStructure> myTreeModel = new StructureTreeModel(treeStructure, disposable);
        return new S3TreeTableModel(new AsyncTreeModel(myTreeModel, true, disposable), columns, myTreeModel);
    }

    private void addTreeActions() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new DownloadObjectAction(project, treeTable));
        actionGroup.add(new UploadObjectAction(project, treeTable));
        actionGroup.add(new Separator());
        actionGroup.add(new NewFolderAction(project, treeTable));
        actionGroup.add(new RenameObjectAction(project, treeTable));
        actionGroup.add(new CopyPathAction(project, treeTable));
        actionGroup.add(new Separator());
        actionGroup.add(new DeleteObjectAction(project, treeTable));
        PopupHandler.installPopupHandler(treeTable, actionGroup, ActionPlaces.EDITOR_POPUP, ActionManager.getInstance());
    }
}
