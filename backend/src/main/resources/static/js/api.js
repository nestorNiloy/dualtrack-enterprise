// Same-origin API client. Spring Security session cookie + CSRF cookie.
const API_BASE = '/api';
function csrfToken(){const m=document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]+)/);return m?decodeURIComponent(m[1]):'';}
async function request(path, options={}){
 const method=(options.method||'GET').toUpperCase();
 const headers={'Accept':'application/json',...(options.body?{'Content-Type':'application/json'}:{}),...(options.headers||{})};
 if(!['GET','HEAD','OPTIONS','TRACE'].includes(method)){const token=csrfToken();if(token)headers['X-XSRF-TOKEN']=token;}
 const res=await fetch(`${API_BASE}${path}`,{credentials:'same-origin',...options,method,headers});
 if(res.status===204)return null;
 const body=await res.text();let data=null;try{data=body?JSON.parse(body):null;}catch{data=body;}
 if(!res.ok){const msg=data?.message||data?.error||`Request failed (${res.status})`;throw new Error(msg);}
 return data;
}
const api={
 csrf:()=>request('/auth/csrf'),
 me:()=>request('/auth/me'),
 login:async(payload)=>{await api.csrf();return request('/auth/login',{method:'POST',body:JSON.stringify(payload)});},
 register:async(payload)=>{await api.csrf();return request('/auth/register',{method:'POST',body:JSON.stringify(payload)});},
 logout:async()=>request('/auth/logout',{method:'POST'}),
 async getAll(){const page=await request('/applications?page=0&size=100');return page.content||[];},
 async create(payload){return request('/applications',{method:'POST',body:JSON.stringify(payload)});},
 async update(id,payload){return request(`/applications/${id}`,{method:'PUT',body:JSON.stringify(payload)});},
 async remove(id){return request(`/applications/${id}`,{method:'DELETE'});},
 stats:()=>request('/applications/stats')
};
