#!/usr/bin/env python3
"""
NASM Instruction Database Management Tool

This script helps manage the instructions.xml database by providing commands to:
- List all instruction names
- Read a specific instruction entry
- Add new instructions (simple or complex, maintaining alphabetical order)
- Edit existing instructions interactively
"""

import xml.etree.ElementTree as ET
import sys
import os
import tempfile
import subprocess
import argparse
from pathlib import Path
from typing import Optional, Dict, List


class InstructionDatabase:
    def __init__(self, xml_path: str = "src/main/resources/nasm/instructions.xml"):
        self.xml_path = Path(xml_path)
        if not self.xml_path.exists():
            raise FileNotFoundError(f"XML file not found: {self.xml_path}")

        self.tree = ET.parse(self.xml_path)
        self.root = self.tree.getroot()

    def list_instructions(self, pattern: Optional[str] = None):
        """List all instruction names, optionally filtered by pattern."""
        instructions = []
        for instruction in self.root.findall('instruction'):
            name = instruction.get('name', '')
            if pattern is None or pattern.lower() in name.lower():
                category = instruction.get('category', 'UNKNOWN')
                instructions.append((name, category))

        instructions.sort()

        print(f"Found {len(instructions)} instruction(s):\n")
        for name, category in instructions:
            print(f"  {name:20} [{category}]")

    def read_instruction(self, name: str):
        """Read and display a complete instruction entry."""
        name_lower = name.lower()

        for instruction in self.root.findall('instruction'):
            if instruction.get('name', '').lower() == name_lower:
                self._print_instruction(instruction)
                return

        print(f"Instruction '{name}' not found in database.")
        print("\nSimilar instructions:")
        self.list_instructions(name[:3])

    def _print_instruction(self, instruction):
        """Pretty print an instruction element."""
        name = instruction.get('name', '')
        category = instruction.get('category', 'UNKNOWN')

        print(f"\nInstruction: {name}")
        print(f"Category: {category}")
        print("-" * 60)

        # Description
        desc = instruction.find('description')
        if desc is not None and desc.text:
            print(f"\nDescription: {desc.text.strip()}")

        # Documentation
        doc = instruction.find('documentation')
        if doc is not None:
            print("\nDocumentation:")
            for elem in doc:
                if elem.text and elem.text.strip():
                    tag_name = elem.tag.replace('-', ' ').title()
                    print(f"  {tag_name}: {elem.text.strip()}")

        # Variants
        variants = instruction.find('variants')
        if variants is not None:
            print("\nVariants:")
            for i, variant in enumerate(variants.findall('variant'), 1):
                operands = [op.get('type', 'UNKNOWN') for op in variant.findall('operand')]
                print(f"  {i}. {' | '.join(operands)}")

        print()

    def add_instruction(self, name: str, category: str, description: str):
        """Add a simple instruction to the database in alphabetical order."""
        name_lower = name.lower()

        # Check if instruction already exists
        if self._find_instruction(name_lower):
            print(f"Error: Instruction '{name}' already exists in database.")
            return False

        # Create new instruction element
        new_instruction = ET.Element('instruction')
        new_instruction.set('name', name_lower)
        new_instruction.set('category', category.upper())

        desc_elem = ET.SubElement(new_instruction, 'description')
        desc_elem.text = description

        # Insert at correct position
        self._insert_instruction(new_instruction, name_lower)

        print(f"Successfully added simple instruction '{name}'")
        return True

    def add_complex_instruction(self, name: str, category: str, description: str,
                                doc_data: Optional[Dict[str, str]] = None,
                                variants: Optional[List[List[str]]] = None):
        """Add a complex instruction with documentation and variants."""
        name_lower = name.lower()

        # Check if instruction already exists
        if self._find_instruction(name_lower):
            print(f"Error: Instruction '{name}' already exists in database.")
            return False

        # Create new instruction element
        new_instruction = ET.Element('instruction')
        new_instruction.set('name', name_lower)
        new_instruction.set('category', category.upper())

        desc_elem = ET.SubElement(new_instruction, 'description')
        desc_elem.text = description

        # Add documentation if provided
        if doc_data:
            doc_elem = ET.SubElement(new_instruction, 'documentation')

            if 'summary' in doc_data:
                summary = ET.SubElement(doc_elem, 'summary')
                summary.text = doc_data['summary']

            if 'description' in doc_data:
                doc_desc = ET.SubElement(doc_elem, 'description')
                doc_desc.text = doc_data['description']

            if 'operation' in doc_data:
                op = ET.SubElement(doc_elem, 'operation')
                op.text = doc_data['operation']

            if 'flags-affected' in doc_data:
                flags = ET.SubElement(doc_elem, 'flags-affected')
                flags.text = doc_data['flags-affected']

            if 'notes' in doc_data:
                notes = ET.SubElement(doc_elem, 'notes')
                notes.text = doc_data['notes']

        # Add variants if provided
        if variants:
            variants_elem = ET.SubElement(new_instruction, 'variants')
            for variant_ops in variants:
                variant = ET.SubElement(variants_elem, 'variant')
                for op_type in variant_ops:
                    operand = ET.SubElement(variant, 'operand')
                    operand.set('type', op_type)

        # Insert at correct position
        self._insert_instruction(new_instruction, name_lower)

        print(f"Successfully added complex instruction '{name}'")
        return True

    def delete_instruction(self, name: str):
        """Delete an instruction from the database."""
        instruction = self._find_instruction(name.lower())
        if instruction is None:
            print(f"Instruction '{name}' not found in database.")
            return False

        self.root.remove(instruction)
        self._indent(self.root)
        self.tree.write(self.xml_path, encoding='UTF-8', xml_declaration=True)

        print(f"Successfully deleted instruction '{name}'")
        return True

    def edit_instruction(self, name: str):
        """Edit an instruction using an interactive text editor."""
        instruction = self._find_instruction(name.lower())
        if instruction is None:
            print(f"Instruction '{name}' not found in database.")
            return False

        # Convert instruction to pretty XML string
        self._indent_element(instruction, 1)
        xml_str = ET.tostring(instruction, encoding='unicode')

        # Create temporary file
        with tempfile.NamedTemporaryFile(mode='w', suffix='.xml', delete=False) as tf:
            tf.write('<?xml version="1.0" encoding="UTF-8"?>\n')
            tf.write('<!-- Edit the instruction below. Save and close to apply changes. -->\n')
            tf.write(xml_str)
            temp_path = tf.name

        try:
            # Open in editor
            editor = os.environ.get('EDITOR', 'vim')
            subprocess.call([editor, temp_path])

            # Read back the edited content
            with open(temp_path, 'r') as f:
                content = f.read()

            # Parse the edited XML
            # Remove XML declaration and comment if present
            lines = [line for line in content.split('\n')
                    if not line.strip().startswith('<?xml')
                    and not line.strip().startswith('<!--')]
            edited_content = '\n'.join(lines)

            new_instruction = ET.fromstring(edited_content)

            # Replace the old instruction with the new one
            parent = self.root
            index = list(parent).index(instruction)
            parent.remove(instruction)

            # Re-insert at correct alphabetical position
            new_name = new_instruction.get('name', '').lower()
            self._insert_instruction(new_instruction, new_name)

            print(f"Successfully updated instruction '{name}'")
            return True

        except ET.ParseError as e:
            print(f"Error parsing edited XML: {e}")
            print(f"Your changes are saved in: {temp_path}")
            return False
        except Exception as e:
            print(f"Error editing instruction: {e}")
            return False
        finally:
            # Clean up temp file if parsing succeeded
            try:
                if os.path.exists(temp_path):
                    os.unlink(temp_path)
            except:
                pass

    def _find_instruction(self, name: str):
        """Find an instruction element by name."""
        for instruction in self.root.findall('instruction'):
            if instruction.get('name', '').lower() == name.lower():
                return instruction
        return None

    def _insert_instruction(self, instruction, name_lower: str):
        """Insert instruction at correct alphabetical position and save."""
        insert_pos = 0
        for i, existing in enumerate(self.root.findall('instruction')):
            if existing.get('name', '') > name_lower:
                break
            insert_pos = i + 1

        self.root.insert(insert_pos, instruction)
        self._indent(self.root)
        self.tree.write(self.xml_path, encoding='UTF-8', xml_declaration=True)

    def patch_instructions(self, patch_xml: str, mode: str = 'merge'):
        """
        Apply a patch to the instruction database.

        Args:
            patch_xml: XML string containing <instruction> or <instructions> element(s)
            mode: 'merge' to recursively merge elements, 'replace' to replace entirely
        """
        # Parse the patch XML
        try:
            patch_root = ET.fromstring(patch_xml)
        except ET.ParseError as e:
            raise ValueError(f"Invalid XML: {e}")

        # Handle both <instruction> and <instructions> root elements
        if patch_root.tag == 'instruction':
            instructions_to_patch = [patch_root]
        elif patch_root.tag == 'instructions':
            instructions_to_patch = patch_root.findall('instruction')
        else:
            raise ValueError(f"Root element must be <instruction> or <instructions>, got <{patch_root.tag}>")

        # Process each instruction in the patch
        results = []
        for patch_inst in instructions_to_patch:
            name = patch_inst.get('name')
            if not name:
                print(f"Warning: Skipping instruction without 'name' attribute")
                continue

            name_lower = name.lower()
            existing = self._find_instruction(name_lower)

            if existing is None:
                # Add new instruction
                self._insert_instruction(patch_inst, name_lower)
                results.append(f"Added: {name}")
            else:
                # Update existing instruction
                if mode == 'replace':
                    # Replace: remove old and insert new
                    self.root.remove(existing)
                    self._insert_instruction(patch_inst, name_lower)
                    results.append(f"Replaced: {name}")
                else:
                    # Merge: recursively merge elements
                    self._merge_elements(existing, patch_inst)
                    self._indent(self.root)
                    self.tree.write(self.xml_path, encoding='UTF-8', xml_declaration=True)
                    results.append(f"Merged: {name}")

        return results

    def _merge_elements(self, target, source):
        """Recursively merge source element into target element."""
        # Update attributes
        for attr, value in source.attrib.items():
            target.set(attr, value)

        # Merge child elements
        for source_child in source:
            if source_child.tag in ['description', 'summary', 'operation', 'flags-affected', 'notes']:
                # These are simple text elements - replace if they exist
                target_child = target.find(source_child.tag)
                if target_child is not None:
                    target.remove(target_child)
                target.append(self._copy_element(source_child))
            elif source_child.tag == 'documentation':
                # Merge documentation children
                target_doc = target.find('documentation')
                if target_doc is None:
                    target.append(self._copy_element(source_child))
                else:
                    self._merge_elements(target_doc, source_child)
            elif source_child.tag == 'variants':
                # Replace variants entirely
                target_variants = target.find('variants')
                if target_variants is not None:
                    target.remove(target_variants)
                target.append(self._copy_element(source_child))
            else:
                # For unknown elements, replace if exists
                target_child = target.find(source_child.tag)
                if target_child is not None:
                    target.remove(target_child)
                target.append(self._copy_element(source_child))

    def _copy_element(self, elem):
        """Deep copy an XML element."""
        new_elem = ET.Element(elem.tag, elem.attrib)
        new_elem.text = elem.text
        new_elem.tail = elem.tail
        for child in elem:
            new_elem.append(self._copy_element(child))
        return new_elem

    def _indent_element(self, elem, level=0):
        """Add proper indentation to a single element."""
        indent = "\n" + "  " * level
        if len(elem):
            if not elem.text or not elem.text.strip():
                elem.text = indent + "  "
            if not elem.tail or not elem.tail.strip():
                elem.tail = indent
            for child in elem:
                self._indent_element(child, level + 1)
            if not child.tail or not child.tail.strip():
                child.tail = indent
        else:
            if level and (not elem.tail or not elem.tail.strip()):
                elem.tail = indent

    def _indent(self, elem, level=0):
        """Add proper indentation to XML for human readability."""
        indent = "\n" + "  " * level
        if len(elem):
            if not elem.text or not elem.text.strip():
                elem.text = indent + "  "
            if not elem.tail or not elem.tail.strip():
                elem.tail = indent
            for child in elem:
                self._indent(child, level + 1)
            if not child.tail or not child.tail.strip():
                child.tail = indent
        else:
            if level and (not elem.tail or not elem.tail.strip()):
                elem.tail = indent


def interactive_add_complex():
    """Interactive prompt for adding a complex instruction."""
    print("Add Complex Instruction - Interactive Mode")
    print("=" * 60)

    # Basic info
    name = input("Instruction name: ").strip()
    if not name:
        print("Error: Name is required")
        return False

    category = input("Category (ARITHMETIC/LOGICAL/DATA_TRANSFER/CONTROL/SYSTEM/etc.): ").strip()
    if not category:
        print("Error: Category is required")
        return False

    description = input("Short description: ").strip()
    if not description:
        print("Error: Description is required")
        return False

    # Documentation (optional)
    print("\nDocumentation (press Enter to skip any field):")
    doc_data = {}

    summary = input("  Summary: ").strip()
    if summary:
        doc_data['summary'] = summary

    doc_desc = input("  Detailed description: ").strip()
    if doc_desc:
        doc_data['description'] = doc_desc

    operation = input("  Operation (e.g., 'DEST := SRC'): ").strip()
    if operation:
        doc_data['operation'] = operation

    flags = input("  Flags affected (e.g., 'OF, SF, ZF, CF'): ").strip()
    if flags:
        doc_data['flags-affected'] = flags

    notes = input("  Notes: ").strip()
    if notes:
        doc_data['notes'] = notes

    # Variants (optional)
    variants = []
    print("\nVariants (press Enter when done):")
    print("Available operand types: REG, R_M, IMM, MEM, LABEL, etc.")

    variant_num = 1
    while True:
        print(f"\n  Variant {variant_num}:")
        variant_ops = []

        while True:
            op = input(f"    Operand {len(variant_ops) + 1} type (or Enter to finish variant): ").strip()
            if not op:
                break
            variant_ops.append(op.upper())

        if not variant_ops:
            break

        variants.append(variant_ops)
        variant_num += 1

    # Confirm and add
    print("\n" + "=" * 60)
    print(f"Adding instruction: {name}")
    print(f"Category: {category}")
    print(f"Description: {description}")
    if doc_data:
        print(f"Documentation fields: {', '.join(doc_data.keys())}")
    if variants:
        print(f"Variants: {len(variants)}")
        for i, v in enumerate(variants, 1):
            print(f"  {i}. {' | '.join(v)}")

    confirm = input("\nAdd this instruction? (y/n): ").strip().lower()
    if confirm != 'y':
        print("Cancelled.")
        return False

    db = InstructionDatabase()
    return db.add_complex_instruction(
        name, category, description,
        doc_data if doc_data else None,
        variants if variants else None
    )


def print_patch_help():
    """Print detailed help for the patch command."""
    print("""
PATCH COMMAND - Detailed Help

The patch command accepts XML from stdin or a file and applies it to the instruction database.

USAGE:
  python instruction_db_tool.py patch [-m MODE] [FILE]
  python instruction_db_tool.py patch [-m MODE] -    # Read from stdin

OPTIONS:
  -m, --mode MODE     Set merge mode: 'merge' (default) or 'replace'
                      - merge: Recursively merge elements, preserving unspecified fields
                      - replace: Replace entire instruction entry

INPUT FORMAT:
  The input XML can be either a single <instruction> element or multiple instructions
  wrapped in an <instructions> element.

INSTRUCTION XML SCHEMA:

  <instruction name="INSTRUCTION_NAME" category="CATEGORY">
    <description>Short description text</description>

    <!-- Optional: Detailed documentation -->
    <documentation>
      <summary>Summary text</summary>
      <description>Detailed description text</description>
      <operation>Operation pseudocode (e.g., "DEST := SRC")</operation>
      <flags-affected>Comma-separated flags (e.g., "OF, SF, ZF, CF")</flags-affected>
      <notes>Additional notes</notes>
    </documentation>

    <!-- Optional: Operand variants -->
    <variants>
      <variant>
        <operand type="REG" />
        <operand type="R_M" />
      </variant>
      <variant>
        <operand type="R_M" />
        <operand type="IMM" />
      </variant>
    </variants>
  </instruction>

ATTRIBUTES:
  - name (required): Instruction mnemonic (will be lowercased)
  - category (required): One of ARITHMETIC, LOGICAL, DATA_TRANSFER, CONTROL, SYSTEM,
                         FLOATING_POINT, SIMD, CRYPTO, etc.

OPERAND TYPES:
  Common types: REG, R_M, IMM, MEM, LABEL, IMM8, IMM16, IMM32, IMM64, REG8, REG16,
                REG32, REG64, SREG (segment register), etc.

MERGE vs REPLACE:
  - MERGE (default): Only elements specified in the patch are updated/added.
                     Existing elements not in the patch are preserved.
                     Perfect for adding documentation to existing instructions.

  - REPLACE: The entire instruction entry is replaced with the patch.
             All existing data is discarded.
             Use when you want complete control over the final result.

EXAMPLES:

  1. Add a simple instruction from stdin:
     echo '<instruction name="nop" category="CONTROL">
       <description>No operation</description>
     </instruction>' | python instruction_db_tool.py patch -

  2. Add documentation to an existing instruction (merge mode):
     cat <<EOF | python instruction_db_tool.py patch -m merge -
     <instruction name="add" category="ARITHMETIC">
       <documentation>
         <notes>This is an additional note that will be merged in</notes>
       </documentation>
     </instruction>
     EOF

  3. Add multiple instructions from a file:
     python instruction_db_tool.py patch new_instructions.xml

  4. Replace an entire instruction entry:
     python instruction_db_tool.py patch -m replace new_syscall.xml

  5. Complete instruction with all fields:
     cat <<EOF | python instruction_db_tool.py patch -
     <instruction name="mov" category="DATA_TRANSFER">
       <description>Move data</description>
       <documentation>
         <summary>Move</summary>
         <description>Copies the source operand to the destination operand.</description>
         <operation>DEST := SRC</operation>
         <flags-affected>None</flags-affected>
       </documentation>
       <variants>
         <variant><operand type="REG" /><operand type="REG" /></variant>
         <variant><operand type="REG" /><operand type="MEM" /></variant>
         <variant><operand type="MEM" /><operand type="REG" /></variant>
         <variant><operand type="REG" /><operand type="IMM" /></variant>
       </variants>
     </instruction>
     EOF

  6. Multiple instructions at once:
     cat <<EOF | python instruction_db_tool.py patch -
     <instructions>
       <instruction name="push" category="CONTROL">
         <description>Push onto stack</description>
       </instruction>
       <instruction name="pop" category="CONTROL">
         <description>Pop from stack</description>
       </instruction>
     </instructions>
     EOF
""")


def main():
    parser = argparse.ArgumentParser(
        description='NASM Instruction Database Management Tool',
        add_help=False
    )

    subparsers = parser.add_subparsers(dest='command', help='Command to execute')

    # List command
    list_parser = subparsers.add_parser('list', help='List instructions')
    list_parser.add_argument('pattern', nargs='?', help='Filter pattern')

    # Read command
    read_parser = subparsers.add_parser('read', help='Read an instruction entry')
    read_parser.add_argument('name', help='Instruction name')

    # Add command
    add_parser = subparsers.add_parser('add', help='Add a simple instruction')
    add_parser.add_argument('name', help='Instruction name')
    add_parser.add_argument('category', help='Instruction category')
    add_parser.add_argument('description', nargs='+', help='Instruction description')

    # Add-complex command
    complex_parser = subparsers.add_parser('add-complex', help='Add a complex instruction interactively')
    complex_parser.add_argument('name', nargs='?', help='Instruction name')
    complex_parser.add_argument('category', nargs='?', help='Instruction category')

    # Edit command
    edit_parser = subparsers.add_parser('edit', help='Edit an instruction in $EDITOR')
    edit_parser.add_argument('name', help='Instruction name')

    # Delete command
    delete_parser = subparsers.add_parser('delete', help='Delete an instruction')
    delete_parser.add_argument('name', help='Instruction name')

    # Patch command
    patch_parser = subparsers.add_parser('patch', help='Patch instructions from XML (use "patch --help" for details)')
    patch_parser.add_argument('file', nargs='?', default='-',
                            help='XML file to patch from (use "-" for stdin)')
    patch_parser.add_argument('-m', '--mode', choices=['merge', 'replace'],
                            default='merge', help='Merge mode (default: merge)')
    patch_parser.add_argument('--help-patch', action='store_true',
                            help='Show detailed help for patch command')

    # Help command
    help_parser = subparsers.add_parser('help', help='Show help')

    # Parse arguments
    if len(sys.argv) < 2:
        parser.print_help()
        print("\nUse 'python instruction_db_tool.py <command> --help' for command-specific help")
        print("Use 'python instruction_db_tool.py patch --help-patch' for detailed patch documentation")
        sys.exit(1)

    args = parser.parse_args()

    try:
        if args.command == 'list':
            db = InstructionDatabase()
            db.list_instructions(args.pattern)

        elif args.command == 'read':
            db = InstructionDatabase()
            db.read_instruction(args.name)

        elif args.command == 'add':
            db = InstructionDatabase()
            description = ' '.join(args.description)
            db.add_instruction(args.name, args.category, description)

        elif args.command == 'add-complex':
            interactive_add_complex()

        elif args.command == 'edit':
            db = InstructionDatabase()
            db.edit_instruction(args.name)

        elif args.command == 'delete':
            db = InstructionDatabase()
            db.delete_instruction(args.name)

        elif args.command == 'patch':
            if hasattr(args, 'help_patch') and args.help_patch:
                print_patch_help()
                sys.exit(0)

            # Read XML from file or stdin
            if args.file == '-':
                xml_content = sys.stdin.read()
            else:
                with open(args.file, 'r') as f:
                    xml_content = f.read()

            db = InstructionDatabase()
            results = db.patch_instructions(xml_content, args.mode)

            print(f"\nPatch applied successfully ({args.mode} mode):")
            for result in results:
                print(f"  {result}")

        elif args.command == 'help' or args.command is None:
            parser.print_help()

        else:
            print(f"Error: Unknown command '{args.command}'")
            parser.print_help()
            sys.exit(1)

    except FileNotFoundError as e:
        print(f"Error: {e}")
        sys.exit(1)
    except ET.ParseError as e:
        print(f"Error parsing XML: {e}")
        sys.exit(1)
    except ValueError as e:
        print(f"Error: {e}")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n\nCancelled by user.")
        sys.exit(1)
    except Exception as e:
        print(f"Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
