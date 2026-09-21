import api from './axios'

/**
 * Baixa um arquivo do storage passando pelo `api`, que anexa o token e renova a
 * sessão no 401. Um `<a href>` direto não leva o JWT: a aba abria em 401 em vez
 * do arquivo.
 *
 * O blob vira um object URL temporário e um `<a download>` sintético dispara o
 * salvamento com o nome original — o `fileKey` sozinho é um UUID sem extensão.
 */
export async function downloadFile(fileKey: string, originalName: string): Promise<void> {
  const res = await api.get(`/files/${fileKey}`, { responseType: 'blob' })

  const url = URL.createObjectURL(res.data as Blob)
  try {
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = originalName
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
  } finally {
    // O revoke não pode ser síncrono: o navegador ainda está lendo o URL quando
    // o clique retorna, e revogar na hora aborta o download.
    setTimeout(() => URL.revokeObjectURL(url), 60_000)
  }
}

/**
 * Nome de arquivo a partir da chave do storage, `{contexto}/{ano}/{mês}/{uuid}-{nome}`.
 * Serve a quem tem a chave mas não guardou o nome — o conteúdo da disciplina, por
 * exemplo, tem `title` ("Apostila") mas não o arquivo ("apostila.pdf"), e salvar
 * sem extensão deixa o arquivo inútil.
 */
export function fileNameFromKey(fileKey: string): string {
  const tail = fileKey.slice(fileKey.lastIndexOf('/') + 1)
  const separator = tail.indexOf('-', 35)
  return separator >= 0 && separator < tail.length - 1 ? tail.slice(separator + 1) : tail
}
