package com.example.model

data class PropTechTemplate(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val iconName: String,
    val defaultPrompt: String,
    val tags: List<String>
)

object PropTechTemplateCatalog {
    val templates = listOf(
        PropTechTemplate(
            id = "deal_underwriting",
            title = "Deal Underwriting & ARV Calculator",
            category = "Underwriting",
            description = "Calculate Maximum Allowable Offer (MAO), 70% rule, estimated repair costs, and projected wholesale fee.",
            iconName = "Calculate",
            tags = listOf("ARV", "Wholesale", "ROI", "MAO"),
            defaultPrompt = "Analyze this off-market property deal:\n- Address: 4824 Oakridge Blvd, Dallas TX\n- Asking Price: $210,000\n- Estimated ARV: $330,000\n- Estimated Rehab: $35,000 (Roof, Kitchen, Flooring)\n- Target Wholesale Assignment Fee: $15,000\n\nPlease calculate MAO, projected ROI for cash buyer, and generate a deal summary card."
        ),
        PropTechTemplate(
            id = "cold_call_script",
            title = "Motivated Seller Cold Call Script",
            category = "Acquisitions",
            description = "High-converting script for distressed properties, tired landlords, tax delinquent, or pre-foreclosure sellers.",
            iconName = "PhoneInTalk",
            tags = listOf("Cold Call", "Script", "Seller Objection", "Lead Gen"),
            defaultPrompt = "Generate a tailored cold calling script for an absentee landlord owner who has a vacant property with code violations in Atlanta, GA. Include an opening hook, 3 qualification questions (timeline, price flexibility, condition), and responses to 'I'm not interested' and 'What's your offer?'."
        ),
        PropTechTemplate(
            id = "wholesale_contract",
            title = "Wholesale Contract & Assignment Clauses",
            category = "Legal & Contracts",
            description = "Draft standard wholesale purchase agreement clauses, inspection contingency periods, and assignment rights.",
            iconName = "Description",
            tags = listOf("Contract", "Assignment", "Legal", "Purchase Agreement"),
            defaultPrompt = "Generate a real estate wholesale Purchase and Sale Agreement assignment clause with standard 14-day inspection contingency, $1,000 earnest money deposit refundable during due diligence, and buyer's right to assign to end-buyer LLC."
        ),
        PropTechTemplate(
            id = "sms_drip_sequence",
            title = "4-Touch Multi-Day SMS Follow-Up Sequence",
            category = "Marketing",
            description = "Automated SMS drip messages for unresponsive or cold leads to revive conversations into scheduled calls.",
            iconName = "Sms",
            tags = listOf("SMS", "Drip Campaign", "Nurture", "Follow Up"),
            defaultPrompt = "Create a 4-step SMS nurture sequence for leads that went cold after receiving an initial cash offer:\n- Day 1: Soft check-in on timeline\n- Day 3: Market comps & local investor interest update\n- Day 7: Creative financing / flexible terms proposal\n- Day 14: Final closing window notice"
        ),
        PropTechTemplate(
            id = "cma_comp_analysis",
            title = "Comparative Market Analysis (CMA) Prompt",
            category = "Valuation",
            description = "Deep comp analysis within 0.5 mile radius, adjusted price/sqft, and neighborhood trajectory assessment.",
            iconName = "Analytics",
            tags = listOf("CMA", "Comps", "Appraisal", "Valuation"),
            defaultPrompt = "Run a Comparative Market Analysis for a 3-bed, 2-bath 1,750 sqft single family home built in 1985 in Phoenix, AZ. Provide price adjustments for pool vs non-pool, 2-car garage vs carport, and calculate median sold price per sqft over the last 90 days."
        ),
        PropTechTemplate(
            id = "repair_estimator",
            title = "Property Rehab & Repair Cost Estimator",
            category = "Rehab & Ops",
            description = "Itemized breakdown for cosmetic, medium, and full-gut renovations by square footage and regional labor tiers.",
            iconName = "HomeRepairService",
            tags = listOf("Rehab", "Repairs", "Labor", "Budget"),
            defaultPrompt = "Provide an itemized repair cost breakdown for a 2,100 sqft ranch home needing cosmetic refresh plus mechanicals:\n- Roof replacement (25 sq)\n- HVAC full system replacement\n- Kitchen cabinets, quartz countertops, new appliances\n- 2 full bathroom updates\n- Interior paint & LVP flooring throughout\nEstimate costs with standard contractor pricing."
        )
    )
}
