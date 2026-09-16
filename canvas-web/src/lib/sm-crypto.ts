/**
 * SM2 加密工具（对接 Snowy 后端密码加密）
 * 依赖 sm-crypto 库
 */
// @ts-expect-error sm-crypto 没有类型声明
import smCrypto from "sm-crypto";

const sm2 = smCrypto.sm2;
const cipherMode = 1; // C1C3C2
const publicKey = "04298364ec840088475eae92a591e01284d1abefcda348b47eb324bb521bb03b0b2a5bc393f6b71dabb8f15c99a0050818b56b23f31743b93df9cf8948f15ddb54";

export function sm2Encrypt(plaintext: string): string {
    return sm2.doEncrypt(plaintext, publicKey, cipherMode);
}
