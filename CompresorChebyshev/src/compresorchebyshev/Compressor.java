/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compresorchebyshev;

import java.io.IOException;

/**
 *
 * @author Miguel Candia
 */
public class Compressor {

    private int GP;
    private int FC;
    private int FE;
    private int muestrasXBloque;
    private Spline spl;
    

    public Compressor(int GP, int FC, int FE) {
        this.GP = GP;
        this.FC = FC;
        this.FE = FE;
        muestrasXBloque = (int) ((GP + 1) * FC * 3) / 2;//((24*(GP+1)*2)/8*FC)/4
        spl = new Spline(muestrasXBloque);
    }

    /**
     * Default constructor
     * @deprecated
     * If nP it's not set with a default value the class makes an exception when executing calcularCoeficientes()
     */
    public Compressor() {
        spl = new Spline();
    }

    public Coeficiente[] calcularCoeficientes(int[] samples, int GP, int FC) {
        this.GP = GP;
        this.FC = FC;
        muestrasXBloque = (int) ((GP + 1) * FC * 3) / 2;//((24*(GP+1)*2)/8*FC)/4
        spl.setpN(muestrasXBloque);
        return calcularCoeficientes(samples);
    }

    public Coeficiente[] calcularCoeficientes(int[] samples) {
        Coeficiente[] result = new Coeficiente[GP + 1];
        double[] coef = new double[GP + 1];
        double[] Ybar, A;
        int i, j;
        Ybar = spl.calcularSpline(samples);
        A = spl.getA();
        coef[0] = Ybar[0];
        for (i = 1; i < muestrasXBloque; i++) {
            coef[0] = coef[0] + Ybar[i];
        }
        coef[0] = coef[0] / muestrasXBloque;

        result[0] = new Coeficiente(coef[0]);
        for (j = 1; j < (GP + 1); j++) {
            coef[j] = 0;
            for (i = 0; i < muestrasXBloque; i++) {
                coef[j] = coef[j] + Ybar[i] * Math.cos((j - 1) * A[i]);
            }
            coef[j] = coef[j] * 2 / muestrasXBloque;
            result[j] = new Coeficiente(coef[j]);
        }
        return result;
    }

    /**
     * Compress the byte block using Chebyshev polynomials
     * @param arreglo
     * @throws IOException
     */
    public Coeficiente[] comprimirBloque(byte[] arreglo) throws IOException {
        byte[] canalDer, canalIzq;
        int i, j;
        int[] muestrasDer, muestrasIzq;
        //Para debuggear
        int numMuestras = (arreglo.length - 44) / 4;
        long tamArchivoOrig = arreglo.length;
        long tamArchivoComp = tamArchivoOrig / FC;

        canalDer = new byte[numMuestras * 2];
        canalIzq = new byte[numMuestras * 2];
        /*
         * Rellenamos los dos arreglos uno por cada canal e invertimos bytes para
         * poder convertir a Integer posteriormente
         */
        i = 44;
        j = 0;
        while (i < arreglo.length - 2) {
            canalIzq[j] = arreglo[i + 1];
            canalIzq[j + 1] = arreglo[i];
            i++;
            canalDer[j] = arreglo[i + 1];
            canalDer[j + 1] = arreglo[i];
            i++;
            j++;
        }

        /*
         * Convertimos los bytes en Integer
         */
        muestrasDer = new int[numMuestras];
        muestrasIzq = new int[numMuestras];
        j = 0;
        for (i = 0; i < canalDer.length; i = i + 2) {
            muestrasDer[j] = bytesAEnteroCompDos(canalDer[i], canalDer[i+1]);
            muestrasIzq[j] = bytesAEnteroCompDos(canalIzq[i], canalIzq[i+1]);
            j++;
        }

        //obtener coeficientes compresor y spline para un bloque
        int[] bloque = new int[muestrasXBloque];
        for (i = 0; i < muestrasXBloque; i++) {
            bloque[i] = muestrasDer[i];
        }
        //Compressor comp = new Compressor(GP, muestrasXBloque);
        Coeficiente[] coef = calcularCoeficientes(bloque);
        for (i = 0; i < coef.length; i++) {
            System.out.println(coef[i]);
        }
        System.out.println("Compresion Finalizada");
        return coef;
        
    }

    /**
     * Convierte 2 bytes en un entero complemento a 2
     * @param b0 primer byte
     * @param b1 segundo byte
     * @return entero
     */
    public int bytesAEnteroCompDos(byte b0, byte b1) {
        int i = 0;
        i |= b0 & 0xFF;
        i <<= 8;
        i |= b1 & 0xFF;
        if (i > 32767) {
            i = i % 32768 - 32768;
        }
        return i;
    }

    public int getMuestrasXBloque() {
        return muestrasXBloque;
    }



}
