package com.keri.emi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

private val Navy = Color(0xFF102B5C)
private val Blue = Color(0xFF2864E8)
private val PaleBlue = Color(0xFFEAF0FF)
private val Ink = Color(0xFF17233B)
private val Muted = Color(0xFF718096)
private val Soft = Color(0xFFF5F7FB)
private val Green = Color(0xFF10A982)
private val Amber = Color(0xFFF2A20A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep system status icons readable and reserve the status-bar inset.
        window.statusBarColor = android.graphics.Color.rgb(245, 247, 251)
        window.navigationBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        setContent { KeRiApp() }
    }
}

private fun rupees(v: Double): String = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    maximumFractionDigits = 0
    minimumFractionDigits = 0
}.format(if (v.isFinite()) v else 0.0)

private fun number(v: Double): String = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
    maximumFractionDigits = 0
}.format(v)

private fun amount(s: String, fallback: Double): Double =
    s.replace(",", "").toDoubleOrNull()?.coerceAtLeast(0.0) ?: fallback

data class YearPayment(val year: Int, val principal: Double, val interest: Double, val balance: Double)
data class LoanSummary(
    val emi: Double, val principal: Double, val interest: Double, val totalEmi: Double,
    val charges: Double, val disbursal: Double, val totalCost: Double, val years: List<YearPayment>
)

fun calculateLoan(principal: Double, annualRate: Double, months: Int, charges: Double): LoanSummary {
    val p = principal.coerceAtLeast(0.0)
    val n = months.coerceAtLeast(1)
    val r = annualRate.coerceAtLeast(0.0) / 1200.0
    val emi = when {
        p == 0.0 -> 0.0
        r == 0.0 -> p / n
        else -> p * r * (1.0 + r).pow(n) / ((1.0 + r).pow(n) - 1.0)
    }
    var balance = p
    var yp = 0.0
    var yi = 0.0
    val rows = mutableListOf<YearPayment>()
    for (month in 1..n) {
        val interest = balance * r
        val paid = (emi - interest).coerceIn(0.0, balance)
        balance = (balance - paid).coerceAtLeast(0.0)
        yp += paid
        yi += interest
        if (month % 12 == 0 || month == n) {
            rows.add(YearPayment((month + 11) / 12, yp, yi, balance))
            yp = 0.0
            yi = 0.0
        }
    }
    val total = emi * n
    return LoanSummary(emi, p, (total - p).coerceAtLeast(0.0), total, charges,
        (p - charges).coerceAtLeast(0.0), total + charges, rows)
}

@Composable
fun KeRiApp() {
    var loanText by remember { mutableStateOf("500000") }
    var rateText by remember { mutableStateOf("9.5") }
    var tenure by remember { mutableIntStateOf(60) }
    var processingText by remember { mutableStateOf("2500") }
    var insuranceText by remember { mutableStateOf("4500") }
    var docsText by remember { mutableStateOf("1000") }
    var showSchedule by remember { mutableStateOf(true) }

    val loan = amount(loanText, 500000.0).coerceIn(10000.0, 100000000.0)
    val rate = amount(rateText, 9.5).coerceIn(0.0, 36.0)
    val fees = amount(processingText, 0.0) + amount(insuranceText, 0.0) + amount(docsText, 0.0)
    val summary = remember(loan, rate, tenure, fees) { calculateLoan(loan, rate, tenure, fees) }

    Column(
        Modifier.fillMaxSize().background(Soft).statusBarsPadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().background(Color.White).padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Navy), contentAlignment = Alignment.Center) {
                    Text("K", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("KeRi EMI Calculator", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Complete Loan & Extra Charges Estimator", color = Muted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Navy).padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("MONTHLY EMI PAYABLE", color = Color(0xFFCFDAF2), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Spacer(Modifier.height(5.dp))
                Text(rupees(summary.emi), color = Color.White, fontSize = 35.sp, fontWeight = FontWeight.ExtraBold)
                Text("for " + tenure + " months", color = Color(0xFFCFDAF2), fontSize = 12.sp)
            }
        }

        PremiumCard("Loan details", "Set the loan amount, rate and repayment period", Icons.Default.Calculate) {
            AmountInput("Loan amount", loanText, { loanText = it }, "₹")
            Slider(value = loan.coerceIn(10000.0, 10000000.0).toFloat(),
                onValueChange = { loanText = it.toInt().toString() }, valueRange = 10000f..10000000f)
            Spacer(Modifier.height(8.dp))
            AmountInput("Interest rate (p.a.)", rateText, { rateText = it }, "%")
            Slider(value = rate.coerceIn(0.0, 24.0).toFloat(),
                onValueChange = { rateText = String.format(Locale.US, "%.2f", it) }, valueRange = 0f..24f)
            Spacer(Modifier.height(8.dp))
            Text("DURATION  ·  " + tenure + " MONTHS", color = Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Slider(value = tenure.toFloat(), onValueChange = { tenure = it.toInt().coerceIn(1, 360) },
                valueRange = 1f..360f, steps = 358)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Custom duration", modifier = Modifier.weight(1f), color = Muted,
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = tenure.toString(),
                    onValueChange = { entered ->
                        if (entered.length <= 3 && (entered.isEmpty() || entered.all(Char::isDigit))) {
                            entered.toIntOrNull()?.let { if (it in 1..360) tenure = it }
                        }
                    },
                    modifier = Modifier.width(148.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End
                    ),
                    suffix = { Text("Mo", color = Muted, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue, unfocusedBorderColor = Color(0xFFDCE3ED),
                        focusedContainerColor = Soft, unfocusedContainerColor = Soft
                    )
                )
            }
            Text("Enter any value from 1 to 360 months.", color = Muted, fontSize = 10.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(12, 24, 36, 60, 120).forEach { m ->
                    val selected = tenure == m
                    Surface(
                        onClick = { tenure = m }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(11.dp),
                        color = if (selected) PaleBlue else Soft,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Blue else Color(0xFFE2E8F0))
                    ) {
                        Text(m.toString() + "M", modifier = Modifier.padding(vertical = 11.dp),
                            textAlign = TextAlign.Center, color = if (selected) Blue else Muted,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        PremiumCard("Additional charges & fees", "Optional · upfront deductions from disbursal", Icons.Default.Payments) {
            AmountInput("File / processing fee", processingText, { processingText = it }, "₹")
            Spacer(Modifier.height(10.dp))
            AmountInput("Loan insurance", insuranceText, { insuranceText = it }, "₹")
            Spacer(Modifier.height(10.dp))
            AmountInput("Documentation & other fees", docsText, { docsText = it }, "₹")
            Spacer(Modifier.height(8.dp))
            Text("Fees are displayed separately and deducted upfront for the in-hand estimate. Actual lender treatment may vary.",
                color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
        }

        PremiumCard("Loan summary", "Understand the complete cost of borrowing", Icons.Default.Calculate) {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Metric("TOTAL INTEREST", rupees(summary.interest), Amber, Modifier.weight(1f))
                Metric("EXTRA CHARGES", rupees(fees), Green, Modifier.weight(1f))
            }
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Metric("IN-HAND DISBURSAL", rupees(summary.disbursal), Ink, Modifier.weight(1f))
                Metric("TOTAL LOAN COST", rupees(summary.totalCost), Blue, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Breakdown(principal = summary.principal, interest = summary.interest, fees = fees)
            HorizontalDivider(color = Color(0xFFE8EDF4))
            SummaryLine("Sanctioned loan amount", rupees(summary.principal))
            SummaryLine("Total EMI payments", rupees(summary.totalEmi))
            SummaryLine("Upfront charges", rupees(fees))
            SummaryLine("Total outflow", rupees(summary.totalCost), true)
            TextButton(onClick = {
                loanText = "500000"; rateText = "9.5"; tenure = 60
                processingText = "2500"; insuranceText = "4500"; docsText = "1000"
            }) { Text("↺  Reset calculator") }
        }

        PremiumCard("Yearly payment schedule", "Principal, interest and closing balance", Icons.Default.Calculate) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Year-wise amortization", modifier = Modifier.weight(1f), color = Ink, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showSchedule = !showSchedule }) { Text(if (showSchedule) "Hide" else "Show") }
            }
            if (showSchedule) {
                Row(Modifier.fillMaxWidth().background(Soft).padding(vertical = 10.dp)) {
                    Cell("YEAR", .5f, true)
                    Cell("PRINCIPAL", 1f, true, TextAlign.End)
                    Cell("INTEREST", 1f, true, TextAlign.End)
                    Cell("BALANCE", 1f, true, TextAlign.End)
                }
                summary.years.forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Cell("Y" + row.year, .5f)
                        Cell(number(row.principal), 1f, false, TextAlign.End)
                        Cell(number(row.interest), 1f, false, TextAlign.End)
                        Cell(number(row.balance), 1f, false, TextAlign.End)
                    }
                    HorizontalDivider(color = Color(0xFFEDF0F5))
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("KeRi EMI Calculator", color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Developed by Shartendu", color = Muted, fontSize = 11.sp)
            Text("Your numbers. Clear decisions.", color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PremiumCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        shape = RoundedCornerShape(21.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(PaleBlue), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Blue, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Muted, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(17.dp))
            content()
        }
    }
}

@Composable
private fun AmountInput(label: String, value: String, onChange: (String) -> Unit, prefix: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = value, onValueChange = { s ->
                if (s.length <= 14 && (s.isEmpty() || s.all { it.isDigit() || it == '.' })) onChange(s)
            },
            modifier = Modifier.width(148.dp), singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
            prefix = { Text(prefix, color = Muted, fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue, unfocusedBorderColor = Color(0xFFDCE3ED),
                focusedContainerColor = Soft, unfocusedContainerColor = Soft
            )
        )
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(Soft).padding(11.dp)) {
        Text(label, color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .3.sp)
        Spacer(Modifier.height(5.dp))
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun Breakdown(principal: Double, interest: Double, fees: Double) {
    val total = (principal + interest + fees).coerceAtLeast(1.0)
    val pieces = listOf(principal / total to Blue, interest / total to Amber, fees / total to Green)
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(132.dp).padding(7.dp)) {
            var start = -90f
            pieces.forEach { piece ->
                val sweep = (piece.first * 360.0).toFloat()
                drawArc(piece.second, start, sweep, false, style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Butt))
                start += sweep
            }
        }
        Spacer(Modifier.width(15.dp))
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Legend("Principal", principal / total, Blue)
            Legend("Interest", interest / total, Amber)
            Legend("Charges", fees / total, Green)
        }
    }
}

@Composable
private fun Legend(label: String, fraction: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(7.dp))
        Text(label + "  " + (fraction * 100).toInt() + "%", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SummaryLine(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = if (bold) Ink else Muted, fontSize = 12.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, color = Ink, fontSize = 13.sp, fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}

@Composable
private fun RowScope.Cell(text: String, weight: Float, header: Boolean = false, align: TextAlign = TextAlign.Start) {
    Text(text, Modifier.weight(weight), color = if (header) Muted else Ink,
        fontSize = if (header) 8.sp else 9.sp, fontWeight = if (header) FontWeight.Bold else FontWeight.Medium,
        textAlign = align, maxLines = 1)
}
