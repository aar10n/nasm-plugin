package dev.agb.nasmplugin.database
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Utility class for loading NASM database information from XML resources.
 * Provides methods to parse instructions, registers, directives, and unified instruction databases from XML files.
 */
object XmlDatabaseLoader {

    /**
     * Safely casts a Node to an Element, returning null if the cast fails
     */
    private fun Node.toElementOrNull(): Element? {
        return this as? Element
    }

    /**
     * Loads registers from an XML resource file.
     *
     * Expected XML structure:
     * ```xml
     * <registers>
     *   <register name="rax" size="BIT_64">
     *     <description>Accumulator register</description>
     *   </register>
     * </registers>
     * ```
     *
     * @param resourcePath The path to the XML resource (e.g., "/nasm/registers.xml")
     * @return List of Register objects parsed from the XML
     * @throws Exception if the XML cannot be parsed or resource is not found
     */
    fun loadRegisters(resourcePath: String): List<RegisterDatabase.Register> {
        val document = loadXmlDocument(resourcePath)
        val registerNodes = document.getElementsByTagName("register")

        return buildList {
            for (i in 0 until registerNodes.length) {
                val element = registerNodes.item(i).toElementOrNull() ?: continue

                val name = element.getAttribute("name")
                val sizeStr = element.getAttribute("size")
                val description = element.getElementsByTagName("description")
                    .item(0)?.textContent?.trim() ?: ""

                // Parse size enum
                val size = try {
                    RegisterDatabase.RegisterSize.valueOf(sizeStr)
                } catch (e: IllegalArgumentException) {
                    RegisterDatabase.RegisterSize.BIT_64 // Default fallback
                }

                add(RegisterDatabase.Register(name, description, size))
            }
        }
    }

    /**
     * Loads directives from an XML resource file.
     *
     * Expected XML structure:
     * ```xml
     * <directives>
     *   <directive name="section">
     *     <description>Define a section</description>
     *   </directive>
     * </directives>
     * ```
     *
     * @param resourcePath The path to the XML resource (e.g., "/nasm/directives.xml")
     * @return List of Directive objects parsed from the XML
     * @throws Exception if the XML cannot be parsed or resource is not found
     */
    fun loadDirectives(resourcePath: String): List<DirectiveDatabase.Directive> {
        val document = loadXmlDocument(resourcePath)
        val directiveNodes = document.getElementsByTagName("directive")

        return buildList {
            for (i in 0 until directiveNodes.length) {
                val element = directiveNodes.item(i).toElementOrNull() ?: continue

                val name = element.getAttribute("name")
                val description = element.getElementsByTagName("description")
                    .item(0)?.textContent?.trim() ?: ""

                add(DirectiveDatabase.Directive(name, description))
            }
        }
    }

    /**
     * Loads preprocessor functions from an XML resource file.
     *
     * Expected XML structure:
     * ```xml
     * <preprocessor-functions>
     *   <function name="%abs">
     *     <description>Return absolute value</description>
     *   </function>
     * </preprocessor-functions>
     * ```
     *
     * @param resourcePath The path to the XML resource (e.g., "/nasm/preprocessor-functions.xml")
     * @return List of PreprocessorFunction objects parsed from the XML
     * @throws Exception if the XML cannot be parsed or resource is not found
     */
    fun loadPreprocessorFunctions(resourcePath: String): List<PreprocessorFunctionDatabase.PreprocessorFunction> {
        val document = loadXmlDocument(resourcePath)
        val functionNodes = document.getElementsByTagName("function")

        return buildList {
            for (i in 0 until functionNodes.length) {
                val element = functionNodes.item(i).toElementOrNull() ?: continue

                val name = element.getAttribute("name")
                val description = element.getElementsByTagName("description")
                    .item(0)?.textContent?.trim() ?: ""

                add(PreprocessorFunctionDatabase.PreprocessorFunction(name, description))
            }
        }
    }

    /**
     * Loads unified instructions from an XML resource file.
     *
     * Expected XML structure:
     * ```xml
     * <instructions>
     *   <instruction name="mov" category="DATA_MOVEMENT">
     *     <description>Move data</description>
     *     <variants>
     *       <variant>
     *         <operand type="REG64"/>
     *         <operand type="REG64"/>
     *       </variant>
     *     </variants>
     *     <documentation>
     *       <summary>Move data from source to destination</summary>
     *       <description>Detailed description...</description>
     *       <operation>DEST := SRC</operation>
     *       <flags-affected>None</flags-affected>
     *       <notes>Additional notes...</notes>
     *     </documentation>
     *   </instruction>
     * </instructions>
     * ```
     *
     * @param resourcePath The path to the XML resource (e.g., "/nasm/instructions-unified.xml")
     * @return List of UnifiedInstruction objects parsed from the XML
     * @throws Exception if the XML cannot be parsed or resource is not found
     */
    fun loadUnifiedInstructions(resourcePath: String): List<InstructionDatabase.UnifiedInstruction> {
        val document = loadXmlDocument(resourcePath)
        val instructionNodes = document.getElementsByTagName("instruction")

        return buildList {
            for (i in 0 until instructionNodes.length) {
                val element = instructionNodes.item(i).toElementOrNull() ?: continue

                val name = element.getAttribute("name")
                val categoryStr = element.getAttribute("category")
                val description = element.getElementsByTagName("description")
                    .item(0)?.textContent?.trim() ?: ""

                // Parse category enum
                val category = try {
                    InstructionDatabase.Category.valueOf(categoryStr)
                } catch (e: IllegalArgumentException) {
                    InstructionDatabase.Category.OTHER
                }

                // Parse variants
                val variants = parseInstructionVariants(element)

                // Parse documentation if present
                val documentation = parseDocumentation(element)

                add(InstructionDatabase.UnifiedInstruction(
                    name = name,
                    description = description,
                    category = category,
                    variants = variants,
                    documentation = documentation
                ))
            }
        }
    }

    /**
     * Parses instruction variants from an instruction element.
     * Returns empty list if no variants are specified.
     */
    private fun parseInstructionVariants(instructionElement: Element): List<InstructionDatabase.InstructionVariant> {
        val variantsNode = instructionElement.getElementsByTagName("variants").item(0)?.toElementOrNull()
            ?: return emptyList()

        val variantNodes = variantsNode.getElementsByTagName("variant")
        return buildList {
            for (i in 0 until variantNodes.length) {
                val variantElement = variantNodes.item(i).toElementOrNull() ?: continue
                val operands = parseOperandSpecs(variantElement)
                add(InstructionDatabase.InstructionVariant(operands))
            }
        }
    }

    /**
     * Parses operand specifications from a variant element.
     */
    private fun parseOperandSpecs(variantElement: Element): List<InstructionDatabase.OperandSpec> {
        val operandNodes = variantElement.getElementsByTagName("operand")
        return buildList {
            for (i in 0 until operandNodes.length) {
                val operandElement = operandNodes.item(i).toElementOrNull() ?: continue
                val typeStr = operandElement.getAttribute("type")
                val optional = operandElement.getAttribute("optional") == "true"

                // Parse operand type enum
                val type = try {
                    InstructionDatabase.OperandType.valueOf(typeStr)
                } catch (e: IllegalArgumentException) {
                    // Try to handle some special cases that might not be in the enum
                    when (typeStr) {
                        "MEM8", "MEM16", "MEM32", "MEM64", "MEM128", "MEM256", "MEM512", "MEM1632", "MEM1664" -> InstructionDatabase.OperandType.MEM
                        "AL", "AX", "EAX", "RAX" -> InstructionDatabase.OperandType.REG
                        "CL", "CX", "ECX", "RCX" -> InstructionDatabase.OperandType.REG
                        "DX", "EDX", "RDX" -> InstructionDatabase.OperandType.REG
                        "IMM1", "IMM3" -> InstructionDatabase.OperandType.IMM
                        "REL", "REL8", "REL16", "REL32" -> InstructionDatabase.OperandType.LABEL
                        "MMX" -> InstructionDatabase.OperandType.MM
                        "ST0", "STI" -> InstructionDatabase.OperandType.ST
                        "MOFFS8", "MOFFS16", "MOFFS32", "MOFFS64" -> InstructionDatabase.OperandType.MEM
                        "CR" -> InstructionDatabase.OperandType.CREG
                        "DR" -> InstructionDatabase.OperandType.DREG
                        "KREG" -> InstructionDatabase.OperandType.ANY // Mask register
                        "UNKNOWN" -> InstructionDatabase.OperandType.ANY
                        else -> {
                            System.err.println("WARNING: Unknown operand type '$typeStr', using ANY")
                            InstructionDatabase.OperandType.ANY
                        }
                    }
                }

                add(InstructionDatabase.OperandSpec(type, optional))
            }
        }
    }

    /**
     * Parses documentation from an instruction element.
     * Returns null if no documentation is present.
     */
    private fun parseDocumentation(instructionElement: Element): InstructionDatabase.InstructionDocumentation? {
        val docNode = instructionElement.getElementsByTagName("documentation").item(0)?.toElementOrNull()
            ?: return null

        val summary = docNode.getElementsByTagName("summary")
            .item(0)?.textContent?.trim() ?: ""
        val description = docNode.getElementsByTagName("description")
            .item(0)?.textContent?.trim() ?: ""
        val operation = docNode.getElementsByTagName("operation")
            .item(0)?.textContent?.trim() ?: ""
        val flagsAffected = docNode.getElementsByTagName("flags-affected")
            .item(0)?.textContent?.trim() ?: ""
        val notes = docNode.getElementsByTagName("notes")
            .item(0)?.textContent?.trim() ?: ""

        return InstructionDatabase.InstructionDocumentation(
            summary = summary,
            description = description,
            operation = operation,
            flagsAffected = flagsAffected,
            notes = notes
        )
    }

    /**
     * Loads an XML document from a resource path.
     *
     * @param resourcePath The classpath resource path to the XML file
     * @return Parsed XML Document
     * @throws Exception if the resource cannot be found or parsed
     */
    private fun loadXmlDocument(resourcePath: String): Document {
        val inputStream: InputStream = this::class.java.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        inputStream.use {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            return builder.parse(it)
        }
    }
}
