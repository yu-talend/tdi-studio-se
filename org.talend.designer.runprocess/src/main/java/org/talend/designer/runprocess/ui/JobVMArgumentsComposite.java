// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jdt.internal.ui.workingsets.WorkingSetMessages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.i18n.Messages;

/**
 * gcui class global comment. Detailled comment <br/>
 * 
 */
public class JobVMArgumentsComposite {

    private static final List<String> EMPTY_STRING_LIST = Collections.unmodifiableList(new ArrayList<String>());

    protected TableViewer viewer;

    private List<String> list = new ArrayList<String>();

    private Composite buttonBox;

    protected Button addButton;

    protected Button removeButton;

    protected Button upButton;

    protected Button downButton;

    protected Button checkBox;

    private RunProcessContext processContext;

    private SelectionListener selectionListener;

    /**
     * DOC gcui RunVMArgumentsViewer constructor comment.
     * 
     * @param name
     * @param labelText
     * @param parent
     */
    public JobVMArgumentsComposite(String name, String labelText, Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelText);

        checkBox = new Button(parent, SWT.CHECK);
        checkBox.setText(EParameterName.JOB_RUN_VM_ARGUMENTS_OPTION.getDisplayName());
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = getNumberOfControls();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 8;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        doFillIntoGrid(composite, layout.numColumns);
        composite.setEnabled(false);
        checkBox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setAllEnabled(composite, checkBox.getSelection());
                if (getJobProcess() != null) {
                    executeCommand(new PropertyChangeCommand((Element) getJobProcess(),
                            EParameterName.JOB_RUN_VM_ARGUMENTS_OPTION.getName(), checkBox.getSelection()));
                }
            }

        });

    }

    private void setAllEnabled(Composite parent, boolean enabled) {
        setControlEnable(parent, enabled);
        if (viewer != null) {
            setControlEnable(viewer.getTable(), enabled);
            if (parent == null) {
                setControlEnable(viewer.getTable().getParent(), enabled);
            }
        }

        setControlEnable(addButton, enabled);
        setControlEnable(removeButton, enabled);
        setControlEnable(upButton, enabled);
        setControlEnable(downButton, enabled);
        selectionChanged();
    }

    protected void doFillIntoGrid(Composite parent, int numColumns) {
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;

        viewer = getTableControl(parent);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalSpan = numColumns - 1;
        gd.grabExcessHorizontalSpace = true;
        viewer.getTable().setLayoutData(gd);

        buttonBox = getButtonBoxControl(parent);
        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        buttonBox.setLayoutData(gd);
    }

    /**
     * Returns this field editor's button box containing the Add, Remove, Up, and Down button.
     * 
     * @param parent the parent control
     * @return the button box
     */
    public Composite getButtonBoxControl(Composite parent) {
        if (buttonBox == null) {
            buttonBox = new Composite(parent, SWT.NULL);
            GridLayout layout = new GridLayout();
            layout.marginWidth = 0;
            buttonBox.setLayout(layout);
            GridData layoutData = new GridData();
            layoutData.widthHint = 100;
            layoutData.minimumWidth = 100;
            buttonBox.setLayoutData(layoutData);
            createButtons(buttonBox);
            buttonBox.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(DisposeEvent event) {
                    addButton = null;
                    removeButton = null;
                    upButton = null;
                    downButton = null;
                    buttonBox = null;
                }
            });

        }
        selectionChanged();
        return buttonBox;
    }

    protected void createButtons(Composite box) {
        addButton = createPushButton(box, WorkingSetMessages.WorkingSetConfigurationDialog_new_label);
        addButton.setEnabled(false);
        removeButton = createPushButton(box, WorkingSetMessages.WorkingSetConfigurationDialog_remove_label);
        removeButton.setEnabled(false);
        upButton = createPushButton(box, WorkingSetMessages.WorkingSetConfigurationDialog_up_label);
        upButton.setEnabled(false);
        downButton = createPushButton(box, WorkingSetMessages.WorkingSetConfigurationDialog_down_label);
        downButton.setEnabled(false);
    }

    protected Button createPushButton(Composite parent, String key) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(key);
        button.setFont(parent.getFont());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        button.setLayoutData(data);
        button.addSelectionListener(getSelectionListener());
        return button;
    }

    private void addPressed() {
        String input = getNewInputObject();
        if (input != null) {
            int index = viewer.getTable().getSelectionIndex();
            if (index >= 0) {
                list.add(index + 1, input);
            } else {
                list.add(input);
            }
            viewer.refresh();
            doSave();
            selectionChanged();
        }
    }

    private void removePressed() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        for (Object obj : selection.toArray()) {
            list.remove(obj);
        }
        viewer.refresh();
        doSave();
        selectionChanged();
    }

    private void upPressed() {
        swap(true);
    }

    private void downPressed() {
        swap(false);
    }

    private void swap(boolean up) {
        int index = viewer.getTable().getSelectionIndex();
        int target = up ? index - 1 : index + 1;

        if (index >= 0) {
            Collections.swap(list, index, target);
        }
        viewer.refresh();
        doSave();
        selectionChanged();
    }

    protected void selectionChanged() {
        if (viewer != null) {
            int index = viewer.getTable().getSelectionIndex();
            int size = viewer.getTable().getItemCount();
            setControlEnable(removeButton, index >= 0);
            setControlEnable(upButton, size > 1 && index > 0);
            setControlEnable(downButton, size > 1 && index >= 0 && index < size - 1);
        }
    }

    /**
     * Creates a selection listener.
     */
    public void createSelectionListener() {
        selectionListener = new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                Widget widget = event.widget;
                if (widget == addButton) {
                    addPressed();
                } else if (widget == removeButton) {
                    removePressed();
                } else if (widget == upButton) {
                    upPressed();
                } else if (widget == downButton) {
                    downPressed();
                } else if (widget == viewer.getTable()) {
                    selectionChanged();
                }
            }
        };
    }

    protected void doSave() {
        String s = writeString(list);
        if (s != null && getJobProcess() != null) {
            executeCommand(new PropertyChangeCommand((Element) getJobProcess(), EParameterName.JOB_RUN_VM_ARGUMENTS.getName(), s));
        }
    }

    private IProcess getJobProcess() {
        if (getProcessContext() != null) {
            return getProcessContext().getProcess();
        }
        return null;
    }

    private void executeCommand(Command cmd) {
        if (cmd != null) {
            boolean exec = false;
            if (processContext != null && processContext.getProcess() instanceof IProcess2) {
                IProcess2 process = (IProcess2) processContext.getProcess();
                if (process != null) {
                    CommandStack commandStack = process.getCommandStack();
                    if (commandStack != null) {
                        exec = true;
                        commandStack.execute(cmd);
                    }
                }
            }
            if (!exec) {
                cmd.execute();
            }
        }
    }

    /**
     * Returns this field editor's list control.
     * 
     * @param parent the parent control
     * @return the list control
     */
    protected TableViewer getTableControl(Composite parent) {
        if (viewer == null) {
            Table table = createTable(parent);
            viewer = new TableViewer(table);
            viewer.setContentProvider(createContentProvider());
            viewer.setLabelProvider(createLabelProvider());
            table.setFont(parent.getFont());
            viewer.addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    JobVMArgumentsComposite.this.selectionChanged();
                }
            });
            viewer.addDoubleClickListener(new IDoubleClickListener() {

                public void doubleClick(DoubleClickEvent event) {
                    editItem(event.getSelection());
                }
            });
        }
        // doLoadDefault();
        return viewer;
    }

    protected void editItem(ISelection sel) {
        IStructuredSelection selection = (IStructuredSelection) sel;
        String existing = (String) selection.getFirstElement();
        String value = getExistingInputObject(existing.replace(" ", ""));
        if (value != null) {
            int indexOf = list.indexOf(existing);
            list.remove(existing);
            list.add(indexOf, value);
            viewer.refresh();
            doSave();
        }
    }

    public int getNumberOfControls() {
        return 2;
    }

    /**
     * Returns this field editor's selection listener. The listener is created if nessessary.
     * 
     * @return the selection listener
     */
    private SelectionListener getSelectionListener() {
        if (selectionListener == null) {
            createSelectionListener();
        }
        return selectionListener;
    }

    protected Shell getShell() {
        if (addButton == null) {
            return null;
        }
        return addButton.getShell();
    }

    protected void setControlEnable(Control control, boolean enable) {
        if (control != null && !control.isDisposed()) {
            control.setEnabled(enable);
        }
    }

    /**
     * Getter for list.
     * 
     * @return the list
     */
    public List<String> getList() {
        return this.list;
    }

    public RunProcessContext getProcessContext() {
        return this.processContext;
    }

    public void setProcessContext(RunProcessContext processContext) {
        this.processContext = processContext;
        list.clear();
        if (getProcessContext() != null && getJobProcess() != null) {
            if (checkBox != null && !checkBox.isDisposed()) {
                IElementParameter param = getJobProcess().getElementParameter(
                        EParameterName.JOB_RUN_VM_ARGUMENTS_OPTION.getName());
                if (param != null && param.getValue() instanceof Boolean && (Boolean) param.getValue()) { // checked
                    checkBox.setSelection(true);
                } else {
                    checkBox.setSelection(false);
                }
                setAllEnabled(null, checkBox.getSelection());
            }
            if (viewer != null && !viewer.getTable().isDisposed()) {
                IElementParameter param = getJobProcess().getElementParameter(EParameterName.JOB_RUN_VM_ARGUMENTS.getName());
                if (param != null && param.getValue() != null) {
                    String s = (String) param.getValue();
                    if (s != null && !"".equals(s)) { //$NON-NLS-1$
                        for (String tmp : readString(s)) {
                            list.add(tmp);
                        }
                    }
                }
            }
        } else {
            if (checkBox != null && !checkBox.isDisposed()) {
                checkBox.setSelection(false);
                setAllEnabled(null, checkBox.getSelection());
            }
        }
        viewer.setInput(list);
    }

    protected IStructuredContentProvider createContentProvider() {
        return new IStructuredContentProvider() {

            public Object[] getElements(Object inputElement) {
                return ((List) inputElement).toArray();
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

        };
    }

    protected ITableLabelProvider createLabelProvider() {
        return new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                String value = ((String) element);
                if (columnIndex == 0) {
                    return value.replace(" ", "");
                }
                throw new IllegalStateException();
            }

            public void addListener(ILabelProviderListener listener) {
            }

            public void dispose() {
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
            }

        };
    }

    protected Table createTable(Composite parent) {
        Table contextTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        contextTable.setLinesVisible(true);
        contextTable.setHeaderVisible(true);

        TableColumn engineName = new TableColumn(contextTable, SWT.NONE);
        engineName.setText(Messages.getString("VMArgumentsViewer.argument")); //$NON-NLS-1$
        engineName.setWidth(200);
        return contextTable;
    }

    protected String getExistingInputObject(String obj) {
        VMArgumentDialog dialog = new VMArgumentDialog(getShell(), obj);
        if (dialog.open() == Dialog.OK) {
            return dialog.getNewItem();
        }
        return obj;
    }

    protected String getNewInputObject() {
        VMArgumentDialog dialog = new VMArgumentDialog(getShell(), null);
        if (dialog.open() == Dialog.OK) {
            return dialog.getNewItem();
        }
        return null;
    }

    protected List<String> readString(String stringList) {
        if (stringList == null || "".equals(stringList)) { //$NON-NLS-1$        
            return EMPTY_STRING_LIST;
        }
        ArrayList<String> result = new ArrayList<String>(50);
        for (String tmp : stringList.split(" ")) {
            if (tmp != null && !"".equals(tmp)) { //$NON-NLS-1$
                result.add(tmp);
            }
        }
        return result;
    }

    protected String writeString(List<String> items) {
        int size = items.size();
        StringBuffer buf = new StringBuffer(size * 50);
        buf.append(" ");
        for (int i = 0; i < size; i++) {
            buf.append(items.get(i).trim().replaceAll(" ", ""));
            buf.append(" "); //$NON-NLS-1$
        }
        return buf.toString();
    }

    protected String getArgumentsString() {
        String argumentsString = writeString(getList());

        return argumentsString;
    }

    /**
     * DOC gcui RunVMArgumentsViewer class global comment. Detailled comment
     */
    class VMArgumentDialog extends Dialog {

        private Text argText;

        private final String arg;

        private String newItem;

        /**
         * DOC gcui VMArgumentDialog constructor comment.
         * 
         * @param parentShell
         * @param item
         */
        protected VMArgumentDialog(Shell parentShell, String item) {
            super(parentShell);
            if (item == null || "".equals(item)) { //$NON-NLS-1$
                arg = ""; //$NON-NLS-1$
            } else {
                arg = item;
            }
            setShellStyle(getShellStyle() | SWT.RESIZE | SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX);
        }

        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.getString("VMArgumentsViewer.vmArgument")); //$NON-NLS-1$
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite container = (Composite) super.createDialogArea(parent);
            container.setLayout(new GridLayout(2, false));
            Label addLabel = new Label(container, SWT.NONE);
            addLabel.setText(Messages.getString("VMArgumentsViewer.argumentcolon")); //$NON-NLS-1$
            argText = new Text(container, SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.heightHint = 50;
            gridData.widthHint = 200;
            argText.setLayoutData(gridData);
            argText.setText(arg);

            return container;
        }

        @Override
        protected void okPressed() {
            newItem = argText.getText();
            super.okPressed();
        }

        public String getNewItem() {
            return newItem;
        }
    }
}
