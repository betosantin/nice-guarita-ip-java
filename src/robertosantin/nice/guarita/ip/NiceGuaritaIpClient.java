package robertosantin.nice.guarita.ip;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NiceGuaritaIpClient {

	private static String enderecoDisp = "192.168.100.68";
	private static int portaDisp = 9000;
	private static String codigoAcesso = "";
	private static int timeout = 4;

	protected static String calculaChecksum(String input) {
		String[] parts = input.split("(?<=\\G..)");
		int cs = 0;
		for (String part : parts) {
			int decimal = Integer.parseInt(part, 16);
			cs += decimal;
		}

		if (cs > 255) {
			cs = cs & 0xFF;
		}

		String csHex = String.format("%02X", cs);
		return input + csHex;
	}

	public static boolean acionaRele(int tipoDisp, int numDisp, int rele, int geraEvt) {
		tipoDisp = Math.min(7, Math.max(1, tipoDisp));
		numDisp = Math.min(7, Math.max(0, numDisp));
		rele = Math.min(4, Math.max(1, rele));
		geraEvt = (geraEvt == 0) ? 0 : 1;

		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(enderecoDisp, portaDisp), timeout * 1000);

			if (!socket.isConnected()) {
				return false;
			}

			if (!codigoAcesso.isEmpty()) {
				OutputStream os = socket.getOutputStream();
				os.write(codigoAcesso.getBytes());
				os.flush();
				socket.getInputStream().read(new byte[12]);
			}

			String message = "000d" + String.format("%02d", tipoDisp) + String.format("%02d", numDisp)
					+ String.format("%02d", rele) + String.format("%02d", geraEvt);
			String checksum = calculaChecksum(message);

			byte[] messageBytes = hexStringToByteArray(checksum);
			OutputStream os = socket.getOutputStream();
			os.write(messageBytes);
			os.flush();
			socket.getInputStream().read(new byte[2]);

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static void main(String[] args) {

		/*
		 * tipoDisp: 1 = TX; 2 = TAG Ativo; 3 = CT; 5 = Biometria; 6 = TAG Passivo; 7 =
		 * Senha
		 */
		/* numDisp: CAN 1 a CAN 8; valores de 0 a 7 */
		/* rele: 1, 2, 3 ou 4 */

//		System.out.print(acionaRele(2, 0, 1, 1)); // Portão de cima
//		System.out.print(acionaRele(2, 1, 2, 1)); // Portão de baixo

//		System.out.print(acionaRele(3, 0, 1, 1)); // Porta frente
		System.out.print(acionaRele(3, 0, 2, 1)); // Porta hall

	}
}