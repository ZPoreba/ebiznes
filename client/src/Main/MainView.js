import React, { Component } from 'react';
import './style.css';
import "antd/dist/antd.css";
import { ShoppingCartOutlined  } from '@ant-design/icons';
import { Button } from 'antd';
import {withRouter} from "react-router-dom";


class MainView extends Component {

    render() {
        return(
            <div className={"mainView"}>
                <ShoppingCartOutlined className={"cartIcon"} style={{fontSize: "150px", color: 'black'}} />
                <div className={"buttonsDiv"}>
                    <Button type="primary"
                            shape="round"
                            style={{marginRight: '20px'}}
                            onClick={e => this.props.history.push('/login')}>Log in</Button>
                    <Button type="primary"
                            shape="round"
                            onClick={e => this.props.history.push('/register')}>Sign up</Button>
                </div>
            </div>)
    }
}

export default withRouter(MainView);