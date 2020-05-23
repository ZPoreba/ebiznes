import React, { Component } from 'react';
import './style.css';
import "antd/dist/antd.css";
import { Card, Spin } from "antd";
import { categoriesService } from './CategoriesService';
import { withRouter } from 'react-router-dom';


class CategoriesView extends Component {

    constructor(props) {
        super(props);

        this.state = {
            categories: [],
            loading: true
        };
    }

    componentDidMount() {
        this.loadCategories();
    }

    loadCategories = () => {
        categoriesService.getCategories().then((resp) => {
            this.setState({categories: resp, loading: false})
        });
    }

    onCategorySelected = (category) => {
        this.props.history.push(`/products/${category}`);
    }

    render() {
        return(
            <div className="categoriesView">
                <Spin spinning={this.state.loading} className='spin' size="large">
                    <Card style={{border: "none"}}>
                        <Card.Grid key="0"
                                   onClick={ e => this.onCategorySelected(0) }
                                   className='gridElement'>all
                        </Card.Grid>
                        {
                            this.state.categories.map( category =>
                            <Card.Grid key={category.id}
                                       onClick={ e => this.onCategorySelected(category.id) }
                                       className='gridElement'>{category.name}
                            </Card.Grid>)
                        }
                    </Card>
                </Spin>
            </div>
        )
    }
}

export default withRouter(CategoriesView);