# Installation Guide

## Engine
1. Download a fresh 13.2 [nightly](https://developer.axonivy.com/download/nightly) engine and unpack it to a directory of choice.
2. Download the latest `smart-core.jar` from the [releases](https://github.com/axonivy/smart-core/releases) page.
3. Copy the downloaded JAR into the `dropins` directory of your engine ![](img/smart-core-dropin-installation.png).

## VS-Code

### Designer extension
1. [Downlod](https://code.visualstudio.com/download) the Visual Studio Code editor (1.104.1 or newer)
2. Install the Axon Ivy PRO Designer 13 (pre-release) [extension](https://marketplace.visualstudio.com/items?itemName=axonivy.vscode-designer-13).
3. Select the previously downloaded engine (with the dropin) as engine directory when being prompted or use the 'settings' button of the extension. ![select-engine](img/smart-core-engine-selection.png)

### MCP tools

1. Open the command bar (CTRL+SHIFT+P)
    - search and launch `MCP: Add Server...`
    - select `HTTP (HTTP or Server-Sent Events)`
    - enter server uri: `http://localhost:8080/smart-core/mcp`
    - enter server ID: `smart-core`

2. Open the Chat window in Agent mode (CTRL+SHIFT+ALT+I)
    - click on the 'hammer and wrench' icon next to your message input.
    - verify that the 'smart-core' MCP server lists tools.
    ![tools](img/smart-core-mcp-tools.png)

3. Done, start chatting. Here's a prompt that we like to use.
    ```
    Create a process.
    Start the process based on a signal, referencing a slack-message from a new customer.
    Add an email element, telling rolf@axonivy.com that we got a new lead!
    Use an alternative gateway, if the predicted license cost is higher than 100K dollars, create a task for marcel with high priority otherwise simply end the process.
    ```

## Caveats

### File changes and editor refresh

The editors in the Axon Ivy PRO Designer VSCode extension currently don't refresh when making changes directly to a file. This means that edits performed by Copilot are not immediatly reflected in the editors. Restart VSCode to make the changes visible.

### Tool usage

Depending on the model you use, Copilot might not always invoke the required tools. If that happens, you can try again, but tell it explicitly to use the respective tools. Alternatively, you can add custom instructions to your project to ensure tool usage by Copilot. To do this, add a file `.github/copilot-instructions.md` with the following content to your project:

```md
Whenever instructed to create, edit, or otherwise work with or handle a file or resource in this project, check the following mapping to determine whether the file/resource is present in it as a key. If so, invoke the corresponding tool defined as the value in the mapping to retrieve a JSON schema and additional guidelines regarding that file/resource before proceeding. Use the schema as the authoritative source of truth for structure, required fields, and allowed values and strictly follow the guidelines returned.

# Tool Mapping
- Process (file extension 'p.json'): ivy-process-schema
- Data Class (file extension 'd.json'): ivy-data-class-schema
- Form (file extension 'f.json'): ivy-form-schema
```

# Troubleshooting

- The MCP server setup lives in the `mcp.json` file. After installating, it should look as follows.

  ```json
  {
  	"servers": {
  		"smart-core-beta": {
  			"url": "http://localhost:8080/smart-core/mcp",
  			"type": "http"
  		}
  	},
  	"inputs": []
  }
  ```

- Ensure that the setting `Chat > Mcp: Access` is set to `all`. Note that if you use an account part of an organization, this setting might be managed by your organization.
