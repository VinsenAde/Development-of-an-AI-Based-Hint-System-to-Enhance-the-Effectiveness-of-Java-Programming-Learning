// ./js/editor-init.js

// This global variable will hold the Monaco Editor instance.
// It's crucial for accessing the editor's content from other modules.
let monacoEditorInstance = null;

export function initEditor() {
    const el = document.getElementById('code-editor');

    // It's good practice to check if Monaco is defined before using it.
    // This helps prevent errors if the Monaco loader hasn't finished its job yet.
    if (typeof monaco === 'undefined' || !monaco.editor) {
        console.error("Monaco object is not yet defined. Cannot initialize editor.");
        // You might want to defer initialization or show an error to the user
        return;
    }

    // Dispose of any existing editor instance before creating a new one
    // This prevents memory leaks and ensures a fresh editor state.
    if (monacoEditorInstance) {
        monacoEditorInstance.dispose();
    }

    // Initialize Monaco Editor
    monacoEditorInstance = monaco.editor.create(el, {
        value: `public class StudentCode {
    public static void main(String[] args) {
        // Write your code here
    }
}
// Declare additional classes below`,
        language: 'java', // Set the language to Java
        theme: 'vs-light', // Use a light theme
        minimap: { enabled: false }, // Disable minimap to save space
        automaticLayout: true, // Editor will resize automatically with its container
        // Additional options you might find useful:
        // scrollbar: {
        //     vertical: 'auto',
        //     horizontal: 'auto',
        //     verticalScrollbarSize: 8,
        //     horizontalScrollbarSize: 8,
        // },
        // fontSize: 14,
        // lineNumbers: 'on',
        // roundedSelection: false,
        // scrollBeyondLastLine: false,
        // readOnly: false, // Set to true to make it read-only
    });

    // Make the instance accessible globally for convenience or other modules
    window.monacoEditorInstance = monacoEditorInstance;
// add this:
window.editor = monacoEditorInstance;
    console.log("Monaco Editor initialized.");
}

// Export a function to get the current code from the editor
export function getEditorCode() {
    if (window.monacoEditorInstance) {
        return window.monacoEditorInstance.getValue();
    }
    return ''; // Return an empty string if the editor hasn't been initialized yet
}