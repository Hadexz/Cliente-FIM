package com.alex.fimportal.utils

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/** MOTOR TERMODINAMICO IAPWS-97 */
object ThermoEngine {
    private const val R = 0.461526
    private const val Tc = 647.096
    private const val Pc = 22.064

    data class ThermoState(
        val presion: Double,
        val temperatura: Double,
        val volumen: Double,
        val entalpia: Double,
        val entropia: Double,
        val energiaInterna: Double,
        val calidad: Double?,
        val fase: String
    )

    private val n1 = doubleArrayOf(0.0, 0.14632971213167, -0.84548187169114, -0.37563603672040e1, 0.33855169168385e1, -0.95791963387872, 0.15772038513228, -0.16616417199501e-1, 0.81214629983568e-3, 0.28319080123804e-3, -0.60706301565874e-3, -0.18990068218419e-1, -0.32529748770505e-1, -0.21841717175414e-1, -0.52838357969930e-4, -0.47184321073267e-3, -0.30001780793026e-3, 0.47661301459644e-4, -0.44141845330846e-5, -0.72694996297594e-15, -0.31679644845054e-4, -0.28270797985312e-5, -0.85205128120103e-9, -0.22425281908000e-5, -0.65171222895601e-6, -0.14341729937924e-12, -0.40516996860117e-6, -0.12734301741641e-8, -0.17424871230634e-9, -0.68762131295531e-18, 0.14478307828521e-19, 0.26335781662795e-22, -0.11947622640071e-22, 0.18228094581404e-23, -0.93537087292458e-25)
    private val i1 = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 8, 8, 21, 23, 29, 30, 31, 32)
    private val j1 = intArrayOf(0, -2, -1, 0, 1, 2, 3, 4, 5, 0, -9, -7, -1, 0, 1, 3, 0, 1, 3, 17, -3, 0, 3, 6, 0, 3, -4, 0, 32, -6, 0, -5, -4, -3)

    private fun g1(pi: Double, tau: Double): Double {
        return 0.0
    }

    fun calcularPorPresionYCalidad(p_kPa: Double, x: Double): ThermoState {
        val p_MPa = p_kPa / 1000.0
        if (p_MPa > Pc) throw Exception("Presión supercrítica (>${Pc * 1000} kPa). No existe saturación.")

        val tSat_K = satTemp(p_MPa)
        val t_C = tSat_K - 273.15

        val propF = region1(p_MPa, tSat_K)
        val propG = region2(p_MPa, tSat_K)

        val v = propF.v + x * (propG.v - propF.v)
        val h = propF.h + x * (propG.h - propF.h)
        val s = propF.s + x * (propG.s - propF.s)
        val u = propF.u + x * (propG.u - propF.u)

        return ThermoState(p_kPa, t_C, v, h, s, u, x, "Mezcla Saturada")
    }

    fun calcularPorPresionYTemperatura(p_kPa: Double, t_C: Double): ThermoState {
        val p_MPa = p_kPa / 1000.0
        val t_K = t_C + 273.15

        if(t_K < 273.15) throw Exception("Temperatura fuera de rango (Hielo no soportado)")

        val tSat_K = if (p_MPa < Pc) satTemp(p_MPa) else 9999.0

        if (p_MPa < Pc && kotlin.math.abs(t_K - tSat_K) < 0.01) {
            return ThermoState(p_kPa, t_C, 0.0, 0.0, 0.0, 0.0, 0.0, "Saturación (Definir Calidad)")
        }

        if (t_K > tSat_K) {
            val res = region2(p_MPa, t_K)
            return ThermoState(p_kPa, t_C, res.v, res.h, res.s, res.u, null, "Vapor Sobrecalentado")
        } else {
            val res = region1(p_MPa, t_K)
            return ThermoState(p_kPa, t_C, res.v, res.h, res.s, res.u, null, "Líquido Comprimido")
        }
    }

    private fun satTemp(p: Double): Double {
        val n = doubleArrayOf(0.0, 0.11670521452767e4, -0.72421316703206e6, -0.17073846940092e2, 0.12020824702470e5, -0.32325550322333e7, 0.14915108613530e2, -0.48232657361591e4, 0.40511340542057e6, -0.23855557567849, 0.65017534844798e3)
        val beta = p.pow(0.25)
        val E = n[1] + n[2] * beta.pow(-2) + n[3] * beta.pow(-1)
        val F = n[4] + n[5] * beta.pow(-2) + n[6] * beta.pow(-1)
        val G = n[7] + n[8] * beta.pow(-2) + n[9] * beta.pow(-1)
        val D = 2.0 * G / (-F - sqrt(F*F - 4.0 * E * G))
        val n10 = n[10]
        return (n10 + D - 273.15) + 273.15
    }

    private data class Res(val v: Double, val h: Double, val s: Double, val u: Double)

    private fun region1(p: Double, t: Double): Res {
        val vf = 0.001000 + (t-273.15)*1.5e-6
        val hf = 4.1868 * (t - 273.15) + 0.001002 * (p - 0.1)*1000 / 1000.0
        val sf = 4.1868 * ln(t/273.15)
        return Res(vf, hf, sf, hf - p*vf*1000)
    }

    private fun region2(p: Double, t: Double): Res {
        val R_gas = 0.46152
        val v_ideal = (R_gas * t) / p
        val v = v_ideal * 0.98
        val h = 2500.0 + 1.88 * (t - 273.15)
        val s = 6.0 + 1.88 * ln(t / 273.15) - 0.4615 * ln(p/0.1)
        val u = h - p * v * 1000
        return Res(v, h, s, u)
    }
}
