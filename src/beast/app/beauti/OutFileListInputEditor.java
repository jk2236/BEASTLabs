package beast.app.beauti;

import beast.app.util.TreeFile;

public class OutFileListInputEditor extends FileListInputEditor {
	private static final long serialVersionUID = 1L;

	public OutFileListInputEditor(BeautiDoc doc) {
		super(doc);
	}
	
    @Override
    public Class<?> baseType() {
		return TreeFile.class;
    }
}
