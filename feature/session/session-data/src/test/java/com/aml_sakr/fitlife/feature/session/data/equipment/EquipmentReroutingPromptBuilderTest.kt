package com.aml_sakr.fitlife.feature.session.data.equipment

import com.google.gson.Gson
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EquipmentReroutingPromptBuilderTest {

    private lateinit var builder: EquipmentReroutingPromptBuilder
    private val gson = Gson()

    @Before
    fun setup() {
        builder = EquipmentReroutingPromptBuilder(gson)
    }

    @Test
    fun `buildPrompt should contain exercise name and equipment`() {
        val exerciseName = "Barbell Squat"
        val equipment = setOf("Dumbbells", "Kettlebell")
        
        val request = builder.buildPrompt(exerciseName, equipment)
        
        val promptText = request.contents.first().parts.first().text
        assertTrue(promptText.contains(exerciseName))
        assertTrue(promptText.contains("Dumbbells"))
        assertTrue(promptText.contains("Kettlebell"))
    }

    @Test
    fun `buildPrompt should request JSON format with correct schema`() {
        val request = builder.buildPrompt("Squat", emptySet())
        
        assertTrue(request.generationConfig.responseMimeType == "application/json")
        val schema = request.generationConfig.responseSchema
        assertTrue(schema.properties.containsKey("alternatives"))
        assertTrue(schema.required.contains("alternatives"))
    }
}
