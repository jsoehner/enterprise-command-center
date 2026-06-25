#!/usr/bin/env python3
import os
import glob
import re

def update_workflow(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Skip if it already contains the setup-node action for Node 24
    if 'uses: actions/setup-node' in content and 'node-version: \'24\'' in content:
        print(f"Skipping {file_path}: Already has Node 24 setup.")
        return

    # Find the checkout step and inject the node setup right after it
    # We look for the end of the checkout step block
    checkout_pattern = re.compile(r'(\s+-\s+name:.*?\n\s+uses:\s*actions/checkout@[^\n]+(?:\n\s+with:.*?(?=\n\s+-|\n\s*$))?)', re.DOTALL)
    
    node_setup_block = """
      - name: Set up Node.js 24
        uses: actions/setup-node@v4
        with:
          node-version: '24'
"""
    
    match = checkout_pattern.search(content)
    if match:
        # Determine indentation level based on the matched checkout step
        indent = match.group(1).split('-')[0]
        
        # Format the block to match the file's indentation
        formatted_block = f"\n{indent}- name: Set up Node.js 24\n{indent}  uses: actions/setup-node@v4\n{indent}  with:\n{indent}    node-version: '24'\n"
        
        new_content = content[:match.end()] + formatted_block + content[match.end():]
        
        with open(file_path, 'w') as f:
            f.write(new_content)
        print(f"Updated {file_path}")
    else:
        print(f"Skipping {file_path}: Could not find actions/checkout step to anchor against.")

def main():
    workflows_dir = os.path.join('.github', 'workflows')
    if not os.path.isdir(workflows_dir):
        print(f"Error: Directory {workflows_dir} not found in the current path.")
        return

    yaml_files = glob.glob(os.path.join(workflows_dir, '*.yml')) + glob.glob(os.path.join(workflows_dir, '*.yaml'))
    
    if not yaml_files:
        print("No workflow files found.")
        return

    for yaml_file in yaml_files:
        update_workflow(yaml_file)

if __name__ == '__main__':
    main()
