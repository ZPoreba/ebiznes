import React, { Component } from 'react';
import { Layout, Menu } from 'antd';
import { Link, withRouter } from 'react-router-dom';
import { commonService } from './CommonService';


const { Header, Content } = Layout;

class CustomLayout extends Component{

    constructor(props) {
        super(props);
        this.state = {
            authenticator: false
        }

        this.props.history.listen((location, action) => {
            if(this.updater.isMounted(this)) this.setCookies();
        });
    }

    logOut = () => {
        commonService.logout().then(resp => {
            this.props.history.push('/login');
        })
    }

    setCookies = () => {
        let cookie = commonService.getCookie('authenticator');
        this.setState({authenticator: cookie});
    }

    renderMenu = () => {
        return(
            <Menu
                theme="dark"
                mode="horizontal" >
                <Menu.Item
                    key="logout"
                    style={{float: 'right'}}>
                    <a onClick={() => this.logOut()} >LOG OUT</a>
                </Menu.Item>
                <Menu.Item
                    key="search"
                    style={{float: 'center'}}>
                    <Link to="/search" >Search</Link>
                </Menu.Item>
                <Menu.Item
                    key="cart"
                    style={{float: 'center'}}>
                    <Link to="/cart" >Cart</Link>
                </Menu.Item>
                <Menu.Item
                    key="wish_list"
                    style={{float: 'center'}}>
                    <Link to="/wishlist" >Wish List</Link>
                </Menu.Item>
                <Menu.Item
                    key="help"
                    style={{float: 'center'}}>
                    <Link to="/profile" >Profile</Link>
                </Menu.Item>
                <Menu.Item
                    key="orders"
                    style={{float: 'center'}}>
                    <Link to="/orders" >Orders</Link>
                </Menu.Item>
                <Menu.Item
                    key="returns"
                    style={{float: 'center'}}>
                    <Link to="/returns" >Returns</Link>
                </Menu.Item>
            </Menu>
        )
    }

    componentDidMount() {
        this.setCookies();
    }

    render(){
        return (
            <Layout>
                <Header style={{
                    position: "fixed",
                    zIndex: 1000,
                    width: "100%"
                }}>
                    <div style={{float: "left"}}>
                        <Link style={{fontFamily: "Times New Roman"}} to="/" onClick={this.onLogoClick} > eBusiness </Link>
                    </div>
                    {
                        this.state.authenticator ?
                        <this.renderMenu/>: null
                    }
                </Header>
                <Content style={{ padding: '0 0px', background: 'none', height: "100%" }}>
                    <div>
                        {this.props.children}
                    </div>
                </Content>
            </Layout>
        );
    }

}

export default withRouter(CustomLayout);