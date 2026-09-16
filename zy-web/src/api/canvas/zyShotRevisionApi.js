import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/shotrevision/` + url, ...arg)

/**
 * 短剧项目分镜版本Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/07 18:51
 **/
export default {
	// 获取短剧项目分镜版本分页
	zyShotRevisionPage(data) {
		return request('page', data, 'get')
	},
	// 提交短剧项目分镜版本表单 edit为true时为编辑，默认为新增
	zyShotRevisionSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除短剧项目分镜版本
	zyShotRevisionDelete(data) {
		return request('delete', data)
	},
	// 获取短剧项目分镜版本详情
	zyShotRevisionDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载短剧项目分镜版本导入模板
    zyShotRevisionDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入短剧项目分镜版本
	zyShotRevisionImport(data) {
		return request('importData', data)
	},
	// 导出短剧项目分镜版本
	zyShotRevisionExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
