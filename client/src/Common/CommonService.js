const API_URL = process.env.REACT_APP_API_URL;

const checkStatus = (response) => {
    if(!response.ok) {
        throw Error(response.status);
    }
    return response;
}

const logout = async () => {

    const fetchData = {
        method: 'GET',
        mode: "cors",
        redirect: 'follow',
        credentials: 'include'
    };

    localStorage.removeItem("user")

    return fetch(`${API_URL}/signOut`, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .catch(error => {
            return {success: false, status: error.message}
        });

}

const getCookie = (name) => {
    let pattern = RegExp(name + "=.[^;]*")
    let matched = document.cookie.match(pattern)
    if(matched){
        let cookie = matched[0].split('=')
        return cookie[1]
    }
    return false
}

const checkIfAuthenticated = async (props) => {
    if(!getCookie('authenticator')) {
        props.history.push(`/login`);
        return false;
    }
    return true;
}

export const commonService = { logout, getCookie, checkIfAuthenticated };